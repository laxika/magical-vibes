package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MOM", collectorNumber = "79")
public class StasisField extends Card {

    public StasisField() {
        target(TargetFilters.creature())
                .addStaticEffect(new SetBasePowerToughnessEffect(0, 2, Subjects.attachedCreature()))
                .addStaticEffect(new GrantKeywordEffect(Keyword.DEFENDER, Subjects.attachedCreature()))
                .addStaticEffect(new LosesAllAbilitiesEffect(Subjects.attachedCreature()));
    }
}
