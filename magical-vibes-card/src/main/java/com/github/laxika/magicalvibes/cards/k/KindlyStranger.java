package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.d.DemonPossessedWitch;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "SOI", collectorNumber = "119")
public class KindlyStranger extends Card {

    public KindlyStranger() {
        setBackFaceCard(new DemonPossessedWitch());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(new TransformSelfEffect()),
                "{2}{B}: Transform this creature."
        ).withActivationCondition(
                new Delirium(),
                "Activate only if there are four or more card types among cards in your graveyard."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "DemonPossessedWitch";
    }
}
