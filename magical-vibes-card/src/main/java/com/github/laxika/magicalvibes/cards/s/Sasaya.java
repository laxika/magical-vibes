package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.CardsInHandMatchingAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.RevealHandCost;
import com.github.laxika.magicalvibes.model.effect.TransformToBackFaceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "145")
public class Sasaya extends Card {

    public Sasaya() {
        setBackFaceCard(new SasayasEssence());

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RevealHandCost(),
                        new ConditionalEffect(
                                new CardsInHandMatchingAtLeast(7, new CardTypePredicate(CardType.LAND)),
                                new TransformToBackFaceEffect())),
                "Reveal your hand: If you have seven or more land cards in your hand, flip Sasaya."));
    }

    @Override
    public String getBackFaceClassName() {
        return "SasayasEssence";
    }
}
