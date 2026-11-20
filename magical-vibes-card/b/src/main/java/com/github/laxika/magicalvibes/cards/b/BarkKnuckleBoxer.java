package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLB", collectorNumber = "164")
public class BarkKnuckleBoxer extends Card {

    public BarkKnuckleBoxer() {
        addTrigger(Triggers.youCastSpell().expend(4), new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, Subjects.source()));
    }
}
