package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SuppressStaticEffectUntilEndOfTurnEffect;

import java.util.List;

@CardRegistration(set = "PCY", collectorNumber = "10")
public class GlitteringLion extends Card {

    public GlitteringLion() {
        addEffect(EffectSlot.STATIC, new PreventAllDamageEffect());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new SuppressStaticEffectUntilEndOfTurnEffect(PreventAllDamageEffect.class)),
                "{3}: Until end of turn, this creature loses \"Prevent all damage that would be dealt to this creature.\" Any player may activate this ability."
        ).withActivatableByAnyPlayer());
    }
}
