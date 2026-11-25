package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOS", collectorNumber = "118")
public class HeatedArgument extends Card {

    public HeatedArgument() {
        // Heated Argument deals 6 damage to target creature. You may exile a card from your
        // graveyard. If you do, Heated Argument also deals 2 damage to that creature's controller.
        target(TargetFilters.creature())
                .addSpellEffect(DealDamageEffect.toTargetCreature(6))
                .addSpellEffect(new MayEffect(
                        SequenceEffect.of(
                                ExileGraveyardCardsEffect.fromOwnGraveyard(1,
                                        new CardNotPredicate(new CardIsSelfPredicate())),
                                new ConditionalEffect(HappenedThisWay.exiled(),
                                        new DealDamageToPlayersEffect(2,
                                                Subjects.controllerOf(Subjects.target())))),
                        "Exile a card from your graveyard?"
                ));
    }
}
