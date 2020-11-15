package cy.jdkdigital.productivebees.common.entity.bee;

import cy.jdkdigital.productivebees.init.ModPointOfInterestTypes;
import cy.jdkdigital.productivebees.init.ModTags;
import cy.jdkdigital.productivebees.util.BeeAttributes;
import net.minecraft.block.Block;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tags.ITag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class SolitaryBeeEntity extends ProductiveBeeEntity
{
    private BlockPos birthNest;
    public boolean hasHadNest = false;

    public SolitaryBeeEntity(EntityType<? extends BeeEntity> entityType, World world) {
        super(entityType, world);
        beehiveInterests = (poiType) -> poiType == ModPointOfInterestTypes.SOLITARY_HIVE.get() || poiType == ModPointOfInterestTypes.SOLITARY_NEST.get();
        beeAttributes.put(BeeAttributes.TYPE, "solitary");
    }

    @Override
    public void tick() {
        super.tick();
    }

    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.removeGoal(this.followParentGoal);
        this.goalSelector.addGoal(3, new HomesickGoal());
    }

    @Nonnull
    @Override
    public EntitySize getSize(Pose poseIn) {
        return super.getSize(poseIn).scale(0.85F);
    }

    @Override
    public int getTimeInHive(boolean hasNectar) {
        // When the bee returns with nectar, it will produce an egg cell and will stay a while
        return hasNectar && !this.isChild() ? 12000 : hasNectar ? 6000 : 1200;
    }

    @Override
    public void writeAdditional(CompoundNBT tag) {
        super.writeAdditional(tag);

        if (birthNest != null) {
            tag.put("birthNest", NBTUtil.writeBlockPos(birthNest));
        }
    }

    @Override
    public void readAdditional(CompoundNBT tag) {
        super.readAdditional(tag);

        if (tag.contains("birthNest")) {
            birthNest = NBTUtil.readBlockPos(tag.getCompound("birthNest"));
        }
    }

    @Override
    public ITag<Block> getNestingTag() {
        return ModTags.SOLITARY_OVERWORLD_NESTS;
    }

    public class HomesickGoal extends Goal {

        @Override
        public boolean shouldExecute() {
            if (SolitaryBeeEntity.this.hasHadNest || SolitaryBeeEntity.this.birthNest == null) {
                return false;
            }
            Vector3d vec3d = Vector3d.copyCenteredHorizontally(SolitaryBeeEntity.this.birthNest);
            double distanceToHome = vec3d.distanceTo(SolitaryBeeEntity.this.getPositionVec());

            return distanceToHome >= 25;
        }

        public void tick() {
            if (SolitaryBeeEntity.this.birthNest != null) {
                if (SolitaryBeeEntity.this.navigator.noPath()) {
                    Vector3d vec3d = Vector3d.copyCenteredHorizontally(SolitaryBeeEntity.this.birthNest);
                    SolitaryBeeEntity.this.navigator.tryMoveToXYZ(vec3d.x, vec3d.y, vec3d.z, 1.0D);
                }
            }
        }
    }
}