package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "240")
public class WandertaleMentor extends Card {

    public WandertaleMentor() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                SpellCastTriggerEffect.wheneverYouExpend(
                        4, List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE))));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.RED, ManaColor.GREEN))),
                "{T}: Add {R} or {G}."
        ));
    }
}
