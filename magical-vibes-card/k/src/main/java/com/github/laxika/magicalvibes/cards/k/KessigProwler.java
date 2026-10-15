package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SinuousPredator;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "EMN", collectorNumber = "163")
public class KessigProwler extends Card {

    public KessigProwler() {
        setBackFaceCard(new SinuousPredator());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{G}",
                List.of(new TransformSelfEffect()),
                "{4}{G}: Transform this creature."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "SinuousPredator";
    }
}
