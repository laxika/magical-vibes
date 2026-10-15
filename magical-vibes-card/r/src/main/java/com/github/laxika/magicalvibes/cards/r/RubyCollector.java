package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.OnceOnlyTriggerEffect;

import java.util.List;

@CardRegistration(set = "YOTJ", collectorNumber = "3")
public class RubyCollector extends Card {

    public RubyCollector() {
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new ConditionalEffect(
                new MinimumAttackers(3),
                new OnceOnlyTriggerEffect(new ConjureCardNamedIntoHandEffect("Mox Ruby", false))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new BoostAllOwnCreaturesEffect(1, 0)),
                "{1}{R}: Creatures you control get +1/+0 until end of turn."));
    }
}
