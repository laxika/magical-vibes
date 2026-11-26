package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THB", collectorNumber = "113")
public class RageScarredBerserker extends Card {

    public RageScarredBerserker() {
        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        BoostEffect.target(1, 0),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, Subjects.target()));
    }
}
