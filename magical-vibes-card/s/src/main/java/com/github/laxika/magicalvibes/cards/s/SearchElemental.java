package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MB2", collectorNumber = "299")
@CardRegistration(set = "MB2", collectorNumber = "535")
public class SearchElemental extends Card {

    public SearchElemental() {
        addTrigger(Triggers.youSearchYourLibrary(), new ScryEffect(1));
        addTrigger(Triggers.youScry(), SequenceEffect.of(
                new PutCountersOnSourceEffect(1, 1, 1),
                new MakeCreatureUnblockableEffect(true)));
    }
}
