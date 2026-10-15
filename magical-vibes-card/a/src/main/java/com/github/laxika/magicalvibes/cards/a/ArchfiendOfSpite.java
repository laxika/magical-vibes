package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerLosesLifeUnlessSacrificesPermanentsEffect;

@CardRegistration(set = "C19", collectorNumber = "14")
public class ArchfiendOfSpite extends Card {

    public ArchfiendOfSpite() {
        addEffect(EffectSlot.ON_DEALT_DAMAGE,
                new DamageSourceControllerLosesLifeUnlessSacrificesPermanentsEffect(0, null));
        addCastingOption(new MadnessCast("{3}{B}{B}"));
    }
}
