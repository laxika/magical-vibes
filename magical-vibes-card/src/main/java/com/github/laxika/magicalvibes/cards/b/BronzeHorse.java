package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToSelfFromTargetingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControllerControlsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "CHR", collectorNumber = "96")
public class BronzeHorse extends Card {

    public BronzeHorse() {
        addEffect(EffectSlot.STATIC, new PreventDamageToSelfFromTargetingSpellsEffect(
                new PermanentControllerControlsPermanentPredicate(new PermanentIsCreaturePredicate(), true)));
    }
}
