package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "EMN", collectorNumber = "175")
public class UlvenwaldCaptive extends Card {

    public UlvenwaldCaptive() {
        setBackFaceCard(new UlvenwaldAbomination());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.GREEN)),
                "{T}: Add {G}."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{G}{G}",
                List.of(new TransformSelfEffect()),
                "{5}{G}{G}: Transform this creature."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "UlvenwaldAbomination";
    }
}
