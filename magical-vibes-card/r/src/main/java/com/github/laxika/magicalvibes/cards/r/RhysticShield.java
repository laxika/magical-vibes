package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;


@CardRegistration(set = "PCY", collectorNumber = "20")
public class RhysticShield extends Card {

    public RhysticShield() {
        // Creatures you control get +0/+1 until end of turn.
        addSpellEffect(BoostEffect.creaturesYouControl(0, 1));

        // They get an additional +0/+2 until end of turn unless any player pays {2}.
        addSpellEffect(UnlessPaysEffect.unlessPays(Subjects.eachPlayer(), "{2}", BoostEffect.creaturesYouControl(0, 2)));
    }
}
