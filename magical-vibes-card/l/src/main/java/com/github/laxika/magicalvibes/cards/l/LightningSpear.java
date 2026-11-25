package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "134")
public class LightningSpear extends Card {

    public LightningSpear() {
        addStaticEffect(new StaticBoostEffect(1, 0, Subjects.attachedCreature()));
        addStaticEffect(new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.attachedCreature()));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(new SacrificeSelfCost(), DealDamageEffect.toAnyTarget(3)),
                "{2}{R}, Sacrifice this Equipment: It deals 3 damage to any target."
        ));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
