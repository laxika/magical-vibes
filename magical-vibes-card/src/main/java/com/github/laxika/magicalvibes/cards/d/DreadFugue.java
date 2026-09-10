package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "107")
public class DreadFugue extends Card {

    public DreadFugue() {
        addCastingOption(AlternateHandCast.cleave("{2}{B}", null));

        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new CastForAlternateCost(),
                new ChooseCardsFromTargetHandEffect(
                        1, List.of(CardType.LAND), new CardMaxManaValuePredicate(2),
                        HandChoiceDestination.DISCARD)));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new NotCondition(new CastForAlternateCost()),
                new ChooseCardsFromTargetHandEffect(1, List.of(CardType.LAND),
                        HandChoiceDestination.DISCARD)));
    }
}
