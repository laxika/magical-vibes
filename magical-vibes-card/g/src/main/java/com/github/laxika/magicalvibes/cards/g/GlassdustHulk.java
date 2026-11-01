package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "ARB", collectorNumber = "7")
@CardRegistration(set = "MM2", collectorNumber = "177")
@CardRegistration(set = "2XM", collectorNumber = "199")
public class GlassdustHulk extends Card {

    public GlassdustHulk() {
        // Whenever another artifact you control enters, this creature gets +1/+1 until end of turn
        // and can't be blocked this turn. Both effects are self-scoped (sourcePermanentId).
        addTrigger(Triggers.enters(Triggers.ARTIFACT, ObjectRelation.ANOTHER_YOU_CONTROL), SequenceEffect.of(
                new BoostSelfEffect(1, 1),
                new MakeCreatureUnblockableEffect(true)));

        // Cycling {W/U} ({W/U}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{W/U}");
    }
}
