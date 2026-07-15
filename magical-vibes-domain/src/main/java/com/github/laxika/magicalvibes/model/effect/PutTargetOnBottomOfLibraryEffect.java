package com.github.laxika.magicalvibes.model.effect;

public record PutTargetOnBottomOfLibraryEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PERMANENT);
    }
}
