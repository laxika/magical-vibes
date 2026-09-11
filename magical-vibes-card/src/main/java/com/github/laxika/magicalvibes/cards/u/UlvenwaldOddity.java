package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "225")
public class UlvenwaldOddity extends Card {

    public UlvenwaldOddity() {
        setBackFaceCard(new UlvenwaldBehemoth());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{G}{G}",
                List.of(new TransformSelfEffect()),
                "{5}{G}{G}: Transform this creature."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "UlvenwaldBehemoth";
    }
}
