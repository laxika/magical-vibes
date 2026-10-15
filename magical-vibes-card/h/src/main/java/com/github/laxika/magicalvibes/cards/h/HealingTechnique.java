package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.DemonstrateEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;

@CardRegistration(set = "C21", collectorNumber = "63")
public class HealingTechnique extends Card {

    public HealingTechnique() {
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .targetGraveyard(true)
                .gainLifeEqualToManaValue(true)
                .build());
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
        addEffect(EffectSlot.ON_SELF_CAST,
                new MayEffect(new DemonstrateEffect(), "Copy Healing Technique?"));
    }
}
