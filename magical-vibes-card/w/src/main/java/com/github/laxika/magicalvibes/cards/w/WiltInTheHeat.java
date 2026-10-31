package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.CardsLeftGraveyardThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MarkTargetCreatureExileInsteadOfDieThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOS", collectorNumber = "243")
public class WiltInTheHeat extends Card {

    public WiltInTheHeat() {
        // This spell costs {2} less to cast if one or more cards left your graveyard this turn.
        addStaticEffect(new ConditionalEffect(
                new CardsLeftGraveyardThisTurn(), new ReduceOwnCastCostEffect(new Fixed(2))));

        // Wilt in the Heat deals 5 damage to target creature. If that creature would die this
        // turn, exile it instead.
        target(TargetFilters.creature())
                .addSpellEffect(new MarkTargetCreatureExileInsteadOfDieThisTurnEffect())
                .addSpellEffect(new DealDamageToTargetCreatureEffect(5));
    }
}
