package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.h.Haggle;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "131")
public class MerchantOfTheVale extends Card {

    public MerchantOfTheVale() {
        setBackFaceCard(new Haggle());
        addCastingOption(new AdventureCast("{R}"));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(new DiscardCardTypeCost(null, null), new DrawCardEffect(1)),
                "{2}{R}, Discard a card: Draw a card."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "Haggle";
    }
}
