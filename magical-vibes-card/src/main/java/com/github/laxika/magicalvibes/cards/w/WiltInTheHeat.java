package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MarkTargetCreatureExileInsteadOfDieThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfCardsLeftGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "SOS", collectorNumber = "243")
public class WiltInTheHeat extends Card {

    public WiltInTheHeat() {
        // This spell costs {2} less to cast if one or more cards left your graveyard this turn.
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostIfCardsLeftGraveyardThisTurnEffect(2));

        // Wilt in the Heat deals 5 damage to target creature. If that creature would die this
        // turn, exile it instead.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                .addEffect(EffectSlot.SPELL, new MarkTargetCreatureExileInsteadOfDieThisTurnEffect())
                .addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(5));
    }
}
