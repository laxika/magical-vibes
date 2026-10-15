package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "199")
public class MaestrosCharm extends Card {

    public MaestrosCharm() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Look at the top five cards of your library. Put one of those cards into your hand and the rest into your graveyard",
                        LookAtTopCardsEffect.chooseExactlyNToHandRestToGraveyard(5, 1)),
                new ChooseOneEffect.ChooseOneOption(
                        "Each opponent loses 3 life and you gain 3 life",
                        List.of(
                                new LoseLifeEffect(3, LoseLifeRecipient.EACH_OPPONENT),
                                new GainLifeEffect(3))),
                new ChooseOneEffect.ChooseOneOption(
                        "Maestros Charm deals 5 damage to target creature or planeswalker",
                        new DealDamageToTargetCreatureOrPlaneswalkerEffect(5),
                        new PermanentPredicateTargetFilter(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentIsPlaneswalkerPredicate())),
                                "Target must be a creature or planeswalker."))
        )));
    }
}
