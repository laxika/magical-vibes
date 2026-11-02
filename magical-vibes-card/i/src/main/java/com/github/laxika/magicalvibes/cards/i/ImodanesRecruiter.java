package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TrainTroops;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOE", collectorNumber = "229")
public class ImodanesRecruiter extends Card {

    public ImodanesRecruiter() {
        setBackFaceCard(new TrainTroops());
        addCastingOption(new AdventureCast("{4}{W}"));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new BoostAllOwnCreaturesEffect(1, 0));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.ALL_OWN_CREATURES));
    }

    @Override
    public String getBackFaceClassName() {
        return "TrainTroops";
    }
}
