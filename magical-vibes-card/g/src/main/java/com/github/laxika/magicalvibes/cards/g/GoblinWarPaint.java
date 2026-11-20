package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M12", collectorNumber = "143")
@CardRegistration(set = "ZEN", collectorNumber = "129")
@CardRegistration(set = "BFZ", collectorNumber = "146")
@CardRegistration(set = "MM2", collectorNumber = "115")
public class GoblinWarPaint extends Card {

    public GoblinWarPaint() {
        target(TargetFilters.creature()).addStaticEffect(new StaticBoostEffect(2, 2, Subjects.attachedCreature()))
                .addStaticEffect(new GrantKeywordEffect(Keyword.HASTE, Subjects.attachedCreature()));
    }
}
