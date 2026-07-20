package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CreatureDiedUnderYourControlThisTurn;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "82")
public class BontuTheGlorified extends Card {

    public BontuTheGlorified() {
        // Bontu can't attack or block unless a creature died under your control this turn.
        addEffect(EffectSlot.STATIC, new CantAttackOrBlockUnlessEffect(
                new CreatureDiedUnderYourControlThisTurn(),
                "a creature died under your control this turn"
        ));

        // {1}{B}, Sacrifice another creature: Scry 1. Each opponent loses 1 life and you gain 1 life.
        addActivatedAbility(new ActivatedAbility(
                false, "{1}{B}",
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        new ScryEffect(1),
                        new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT),
                        new GainLifeEffect(1)
                ),
                "{1}{B}, Sacrifice another creature: Scry 1. Each opponent loses 1 life and you gain 1 life."
        ));
    }
}
