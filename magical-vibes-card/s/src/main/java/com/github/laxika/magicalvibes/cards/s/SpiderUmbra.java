package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TotemArmorEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ROE", collectorNumber = "208")
@CardRegistration(set = "UMA", collectorNumber = "184")
@CardRegistration(set = "ARC", collectorNumber = "70")
public class SpiderUmbra extends Card {

    public SpiderUmbra() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(1, 1, Subjects.attachedCreature()))
                .addStaticEffect(new GrantKeywordEffect(Keyword.REACH, Subjects.attachedCreature()))
                .addStaticEffect(new TotemArmorEffect());
    }
}
