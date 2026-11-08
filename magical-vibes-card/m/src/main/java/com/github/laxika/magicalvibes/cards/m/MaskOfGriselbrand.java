package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.amount.TriggeringPermanentPower;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEqualToAmountEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "MID", collectorNumber = "111")
@CardRegistration(set = "DBL", collectorNumber = "111")
@CardRegistration(set = "DSC", collectorNumber = "145")
public class MaskOfGriselbrand extends Card {

    public MaskOfGriselbrand() {
        addStaticEffect(new GrantKeywordEffect(Set.of(Keyword.FLYING, Keyword.LIFELINK), GrantScope.EQUIPPED_CREATURE));
        // Whenever equipped creature dies, you may pay X life, where X is its power. If you do, draw X cards.
        addTrigger(Triggers.creatureDies(ObjectRelation.EQUIPPED),
                new MayPayLifeEqualToAmountEffect(new TriggeringPermanentPower(),
                        new DrawCardEffect(new TriggeringPermanentPower()),
                        "Pay life equal to its power to draw that many cards?"));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
