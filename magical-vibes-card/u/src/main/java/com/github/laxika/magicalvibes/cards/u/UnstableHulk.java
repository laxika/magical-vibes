package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipKind;
import com.github.laxika.magicalvibes.model.effect.SkipNextEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LGN", collectorNumber = "115")
public class UnstableHulk extends Card {

    public UnstableHulk() {
        addMorph("{3}{R}{R}");
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS), SequenceEffect.of(
                new BoostSelfEffect(6, 6),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF),
                new SkipNextEffect(SkipKind.TURN)));
    }
}
