package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MB2", collectorNumber = "299")
@CardRegistration(set = "MB2", collectorNumber = "535")
public class SearchElemental extends Card {

    public SearchElemental() {
        addTrigger(Triggers.youSearchYourLibrary(), new ScryEffect(1));
        addTrigger(Triggers.youScry(), SequenceEffect.of(
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE),
                MakeCreatureUnblockableEffect.self()));
    }
}
