package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectRole;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "30")
public class Glarecaster extends Card {

    public Glarecaster() {
        addActivatedAbility(new ActivatedAbility(false, "{5}{W}",
                List.of(RedirectNextDamageEffect.nextEvent(
                        RedirectRole.SOURCE_PERMANENT_AND_CONTROLLER,
                        RedirectRole.TARGET, TargetPredicates.anyTarget())),
                "{5}{W}: The next time damage would be dealt to this creature and/or you this turn, "
                        + "that damage is dealt to any target instead."));
    }
}
