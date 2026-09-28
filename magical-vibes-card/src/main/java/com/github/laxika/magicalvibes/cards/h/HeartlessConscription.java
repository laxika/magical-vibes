package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "OTC", collectorNumber = "21")
@CardRegistration(set = "OTC", collectorNumber = "57")
public class HeartlessConscription extends Card {

    public HeartlessConscription() {
        addEffect(EffectSlot.SPELL, ExileAllPermanentsEffect.withControllerPlayPermission(
                new PermanentIsCreaturePredicate(), true));
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
