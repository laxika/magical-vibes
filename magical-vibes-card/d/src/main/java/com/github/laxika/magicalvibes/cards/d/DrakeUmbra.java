package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TotemArmorEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ROE", collectorNumber = "63")
public class DrakeUmbra extends Card {

    public DrakeUmbra() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(3, 3, Subjects.attachedCreature()))
                .addStaticEffect(new GrantKeywordEffect(Keyword.FLYING, Subjects.attachedCreature()))
                .addStaticEffect(new TotemArmorEffect());
    }
}
