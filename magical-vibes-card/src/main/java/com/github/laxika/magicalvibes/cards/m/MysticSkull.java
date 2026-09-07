package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "256")
public class MysticSkull extends Card {

    public MysticSkull() {
        setBackFaceCard(new MysticMonstrosity());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new AwardAnyColorManaEffect()),
                "{1}, {T}: Add one mana of any color."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(new TransformSelfEffect()),
                "{5}, {T}: Transform this artifact."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "MysticMonstrosity";
    }
}
