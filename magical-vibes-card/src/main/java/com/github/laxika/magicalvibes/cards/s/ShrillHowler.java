package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.h.HowlingChorus;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesWithLessPowerEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "214")
public class ShrillHowler extends Card {

    public ShrillHowler() {
        HowlingChorus backFace = new HowlingChorus();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // Creatures with power less than this creature's power can't block it.
        addEffect(EffectSlot.STATIC, new CantBeBlockedByCreaturesWithLessPowerEffect());

        // {5}{G}: Transform this creature.
        addActivatedAbility(new ActivatedAbility(
                false, "{5}{G}",
                List.of(new TransformSelfEffect()),
                "{5}{G}: Transform this creature."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "HowlingChorus";
    }
}
