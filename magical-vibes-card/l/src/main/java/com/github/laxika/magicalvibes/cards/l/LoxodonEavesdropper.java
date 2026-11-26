package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "168")
public class LoxodonEavesdropper extends Card {

    public LoxodonEavesdropper() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), CreateTokenEffect.ofClueToken(1));
        addTrigger(Triggers.youDrawSecondCardEachTurn(),
                SequenceEffect.of(
                        BoostEffect.self(1, 1),
                        new GrantKeywordEffect(Keyword.VIGILANCE, Subjects.source())));
    }
}
