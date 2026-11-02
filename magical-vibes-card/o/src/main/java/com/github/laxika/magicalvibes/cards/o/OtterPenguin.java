package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLA", collectorNumber = "67")
public class OtterPenguin extends Card {

    public OtterPenguin() {
        addTrigger(Triggers.youDrawSecondCardEachTurn(),
                SequenceEffect.of(
                        new BoostSelfEffect(1, 2),
                        new MakeCreatureUnblockableEffect(true)));
    }
}
