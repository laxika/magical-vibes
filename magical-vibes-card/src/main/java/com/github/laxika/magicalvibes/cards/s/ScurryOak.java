package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EvolveTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MH2", collectorNumber = "172")
public class ScurryOak extends Card {

    public ScurryOak() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new EvolveTriggerEffect());
        addEffect(EffectSlot.ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT,
                new MayEffect(
                        new CreateTokenEffect("Squirrel", 1, 1, CardColor.GREEN,
                                List.of(CardSubtype.SQUIRREL), Set.of(), Set.of()),
                        "Create a 1/1 green Squirrel creature token?"));
    }
}
