package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.JumpStartCast;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PlaneswalkEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "WHO", collectorNumber = "56")
@CardRegistration(set = "WHO", collectorNumber = "661")
public class StartTheTARDIS extends Card {

    public StartTheTARDIS() {
        addSpellEffect(new SurveilEffect(2));
        addSpellEffect(new DrawCardEffect(1));
        addSpellEffect(new MayEffect(new PlaneswalkEffect(), "Planeswalk?"));
        addCastingOption(new JumpStartCast());
    }
}
