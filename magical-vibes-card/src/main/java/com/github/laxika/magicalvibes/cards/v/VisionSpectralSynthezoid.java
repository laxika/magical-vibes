package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "644")
public class VisionSpectralSynthezoid extends Card {

    public VisionSpectralSynthezoid() {
        addEffect(EffectSlot.STATIC, new AlternativeCostForSpellsEffect(
                "{0}",
                new CardAnyOfPredicate(List.of(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                        new CardSubtypePredicate(CardSubtype.ROBOT))),
                null,
                true,
                true,
                false,
                false,
                true));
    }
}
