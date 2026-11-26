package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THS", collectorNumber = "123")
public class FlamespeakerAdept extends Card {

    public FlamespeakerAdept() {
        addTrigger(Triggers.youScry(), SequenceEffect.of(
                BoostEffect.self(2, 0),
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, Subjects.source())));
    }
}
