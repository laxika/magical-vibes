package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourceIntensity;
import com.github.laxika.magicalvibes.model.effect.InitializeSourceIntensityEffect;
import com.github.laxika.magicalvibes.model.effect.IntensifyCardsOfSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsFromControllerGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YLCI", collectorNumber = "2")
public class LegionsChant extends Card {

    public LegionsChant() {
        setSubtypes(List.of(CardSubtype.CHORUS));
        addEffect(EffectSlot.SPELL, new InitializeSourceIntensityEffect(3));
        addEffect(EffectSlot.SPELL,
                ReturnCardsFromControllerGraveyardToBattlefieldEffect.withinTotalManaValue(
                        new CardTypePredicate(CardType.CREATURE), new SourceIntensity()));
        addEffect(EffectSlot.SPELL, new IntensifyCardsOfSubtypeEffect(CardSubtype.CHORUS));
    }
}
