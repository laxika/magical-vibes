package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "78")
public class BrinelinTheMoonKraken extends Card {

    public BrinelinTheMoonKraken() {
        target(TargetFilters.nonlandPermanent())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new MayEffect(ReturnToHandEffect.target(),
                                "Return target nonland permanent to its owner's hand?"));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new MayEffect(
                        new SpellCastTriggerEffect(
                                new CardMinManaValuePredicate(6, true),
                                List.of(ReturnToHandEffect.target())),
                        "Return target nonland permanent to its owner's hand?"));
    }
}
