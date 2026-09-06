package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.f.FetchQuest;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "164")
public class BrambleFamiliar extends Card {

    public BrambleFamiliar() {
        setBackFaceCard(new FetchQuest());
        addCastingOption(new AdventureCast("{5}{G}{G}"));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{G}",
                List.of(new DiscardCardTypeCost(null, null), ReturnToHandEffect.self()),
                "{1}{G}, {T}, Discard a card: Return this creature to its owner's hand."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "FetchQuest";
    }
}
