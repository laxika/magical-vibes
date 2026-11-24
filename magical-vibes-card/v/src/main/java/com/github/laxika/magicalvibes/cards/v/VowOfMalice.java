package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantAttackCardOwnerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "CMD", collectorNumber = "107")
@CardRegistration(set = "C15", collectorNumber = "140")
@CardRegistration(set = "CMA", collectorNumber = "73")
@CardRegistration(set = "CM2", collectorNumber = "82")
public class VowOfMalice extends Card {

    public VowOfMalice() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(2, 2, Subjects.attachedCreature()))
                .addStaticEffect(new GrantKeywordEffect(Keyword.INTIMIDATE, Subjects.attachedCreature()))
                .addStaticEffect(CantAttackCardOwnerEffect.enchantedCreatureCantAttack(Subjects.you(), true));
    }
}
