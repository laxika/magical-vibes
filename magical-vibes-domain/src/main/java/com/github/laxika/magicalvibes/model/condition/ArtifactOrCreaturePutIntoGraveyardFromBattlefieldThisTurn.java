package com.github.laxika.magicalvibes.model.condition;

/** An artifact or creature was put into a graveyard from the battlefield this turn. */
public record ArtifactOrCreaturePutIntoGraveyardFromBattlefieldThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "an artifact or creature was put into a graveyard from the battlefield this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no artifact or creature was put into a graveyard from the battlefield this turn";
    }
}
