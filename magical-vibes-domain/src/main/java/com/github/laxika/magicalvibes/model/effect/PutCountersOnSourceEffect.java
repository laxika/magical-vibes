package com.github.laxika.magicalvibes.model.effect;

public record PutCountersOnSourceEffect(int powerModifier, int toughnessModifier, int amount,
                                        int controllerDrawCount)
        implements CombatDamageTriggerContextEffect {

    public PutCountersOnSourceEffect(int powerModifier, int toughnessModifier, int amount) {
        this(powerModifier, toughnessModifier, amount, 0);
    }

    public static PutCountersOnSourceEffect onSecondControllerDraw() {
        return new PutCountersOnSourceEffect(1, 1, 1, 2);
    }

    @Override
    public boolean triggersOnControllerDrawCount(int cardsDrawnThisTurn) {
        return controllerDrawCount == 0 || controllerDrawCount == cardsDrawnThisTurn;
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.SOURCE_SELF;
    }
}
