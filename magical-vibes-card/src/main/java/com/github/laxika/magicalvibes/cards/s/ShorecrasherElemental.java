package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnTurnFaceUpEffect;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "73")
public class ShorecrasherElemental extends Card {

    public ShorecrasherElemental() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(FlickerEffect.flickerSelfFaceDown()),
                "{U}: Exile this creature, then return it to the battlefield face down under its owner's control."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "This creature gets +1/-1 until end of turn",
                                new BoostSelfEffect(1, -1)),
                        new ChooseOneEffect.ChooseOneOption(
                                "This creature gets -1/+1 until end of turn",
                                new BoostSelfEffect(-1, 1))
                ))),
                "{1}: This creature gets +1/-1 or -1/+1 until end of turn."
        ).withModalChoiceAtActivation());

        addMorph("{4}{U}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new PutCountersOnTurnFaceUpEffect(1));
    }
}
