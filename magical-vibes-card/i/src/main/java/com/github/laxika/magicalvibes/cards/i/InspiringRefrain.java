package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "216")
@CardRegistration(set = "WHO", collectorNumber = "470")
@CardRegistration(set = "WHO", collectorNumber = "807")
@CardRegistration(set = "WHO", collectorNumber = "1061")
@CardRegistration(set = "C21", collectorNumber = "27")
public class InspiringRefrain extends Card {

    public InspiringRefrain() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
        addEffect(EffectSlot.SPELL, new ExileSpellEffect(3));
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(),
                "Suspend 3—{2}{U}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(3));
    }
}
