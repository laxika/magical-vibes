package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "SOI", collectorNumber = "266")
public class ThrabenGargoyle extends Card {

    public ThrabenGargoyle() {
        setBackFaceCard(new StonewingAntagonizer());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{6}",
                List.of(new TransformSelfEffect()),
                "{6}: Transform this creature."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "StonewingAntagonizer";
    }
}
