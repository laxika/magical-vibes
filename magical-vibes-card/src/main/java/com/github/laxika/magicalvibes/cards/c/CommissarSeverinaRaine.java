package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import java.util.List;

@CardRegistration(set = "40K", collectorNumber = "112")
public class CommissarSeverinaRaine extends Card {

    public CommissarSeverinaRaine() {
        // Whenever Commissar Severina Raine attacks, each opponent loses X life, where X is the
        // number of other attacking creatures.
        PermanentCount otherAttackingCreatures = new PermanentCount(
                new PermanentIsAttackingPredicate(), CountScope.ANY_PLAYER, true);
        addEffect(EffectSlot.ON_ATTACK,
                new LoseLifeEffect(otherAttackingCreatures, LoseLifeRecipient.EACH_OPPONENT));

        // {2}, Sacrifice another creature: You gain 2 life and draw a card.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        new GainLifeEffect(2),
                        new DrawCardEffect(1)
                ),
                "{2}, Sacrifice another creature: You gain 2 life and draw a card."
        ));
    }
}
