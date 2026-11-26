package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TrainTroops;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOE", collectorNumber = "229")
public class ImodanesRecruiter extends Card {

    public ImodanesRecruiter() {
        setBackFaceCard(new TrainTroops());
        addCastingOption(new AdventureCast("{4}{W}"));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                BoostEffect.creaturesYouControl(1, 0),
                new GrantKeywordEffect(Keyword.HASTE, Subjects.creaturesYouControl()));
    }

    @Override
    public String getBackFaceClassName() {
        return "TrainTroops";
    }
}
