package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnUpToOneOfEachFilterFromGraveyardToDestinationsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "C21", collectorNumber = "74")
public class RevivalExperiment extends Card {

    public RevivalExperiment() {
        addEffect(EffectSlot.SPELL, new ReturnUpToOneOfEachFilterFromGraveyardToDestinationsEffect(
                List.of(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new CardTypePredicate(CardType.CREATURE),
                        new CardTypePredicate(CardType.ENCHANTMENT),
                        new CardTypePredicate(CardType.LAND),
                        new CardTypePredicate(CardType.PLANESWALKER)),
                List.of(
                        GraveyardChoiceDestination.BATTLEFIELD,
                        GraveyardChoiceDestination.BATTLEFIELD,
                        GraveyardChoiceDestination.BATTLEFIELD,
                        GraveyardChoiceDestination.BATTLEFIELD,
                        GraveyardChoiceDestination.BATTLEFIELD),
                List.of("artifact card", "creature card", "enchantment card", "land card", "planeswalker card")));
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(
                new Scaled(new EventValue(), 3), LoseLifeRecipient.CONTROLLER));
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
