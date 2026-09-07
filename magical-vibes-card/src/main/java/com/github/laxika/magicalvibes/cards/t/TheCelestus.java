package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.BecomeDayAsEntersEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ToggleDayNightEffect;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "252")
public class TheCelestus extends Card {

    public TheCelestus() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomeDayAsEntersEffect());
        addActivatedAbility(ManaAbilities.tapForAnyColor());
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new ToggleDayNightEffect()),
                "{3}, {T}: If it's night, it becomes day. Otherwise, it becomes night. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
        addEffect(EffectSlot.ON_DAY_NIGHT_CHANGE, SequenceEffect.of(
                new GainLifeEffect(1),
                new MayEffect(
                        SequenceEffect.of(
                                new DrawCardEffect(1),
                                new DiscardEffect(1, DiscardRecipient.CONTROLLER)
                        ),
                        "Draw a card, then discard a card?"
                )
        ));
    }
}
