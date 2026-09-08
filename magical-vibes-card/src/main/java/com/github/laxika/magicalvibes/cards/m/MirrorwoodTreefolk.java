package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectRole;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;

import java.util.List;

@CardRegistration(set = "PLS", collectorNumber = "83")
public class MirrorwoodTreefolk extends Card {

    public MirrorwoodTreefolk() {
        addActivatedAbility(new ActivatedAbility(false, "{2}{R}{W}",
                List.of(RedirectNextDamageEffect.nextEvent(RedirectRole.SOURCE_PERMANENT,
                        RedirectRole.TARGET, TargetPredicates.anyTarget())),
                "{2}{R}{W}: The next time damage would be dealt to this creature this turn, that damage is dealt to any target instead."));
    }
}
