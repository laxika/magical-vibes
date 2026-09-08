package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.g.Granted;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "44")
public class FaeOfWishes extends Card {

    public FaeOfWishes() {
        setBackFaceCard(new Granted());
        addCastingOption(new AdventureCast("{3}{U}"));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(
                        new DiscardCardTypeCost(null, null, 2),
                        ReturnToHandEffect.self()
                ),
                "{1}{U}, Discard two cards: Return Fae of Wishes to its owner's hand."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "Granted";
    }
}
