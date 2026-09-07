package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.h.HideousFleshwheeler;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "119")
public class NezumiFreewheeler extends Card {

    public NezumiFreewheeler() {
        setBackFaceCard(new HideousFleshwheeler());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                new MillEffect(3, MillRecipient.CONTROLLER),
                new MillEffect(3, MillRecipient.EACH_OPPONENT)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{W/P}",
                List.of(new TransformSelfEffect()),
                "{5}{W/P}: Transform Nezumi Freewheeler. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "HideousFleshwheeler";
    }
}
