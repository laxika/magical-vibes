package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.v.ValorsReachTagTeam;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "MOM", collectorNumber = "235")
public class InvasionOfKylem extends Card {

    public InvasionOfKylem() {
        setBackFaceCard(new ValorsReachTagTeam());

        target(TargetFilters.creature(), 0, 2)
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new BoostTargetCreatureEffect(2, 0))
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new GrantKeywordEffect(Set.of(Keyword.VIGILANCE, Keyword.HASTE), GrantScope.TARGET));
    }

    @Override
    public String getBackFaceClassName() {
        return "ValorsReachTagTeam";
    }
}
