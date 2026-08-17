package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.i.InsidiousMist;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "SOI", collectorNumber = "108")
public class ElusiveTormentor extends Card {

    public ElusiveTormentor() {
        setBackFaceCard(new InsidiousMist());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new DiscardCardTypeCost(null, null), new TransformSelfEffect()),
                "{1}, Discard a card: Transform this creature."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "InsidiousMist";
    }
}
