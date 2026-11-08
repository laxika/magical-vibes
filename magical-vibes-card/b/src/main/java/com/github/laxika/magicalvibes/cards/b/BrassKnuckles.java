package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.EquippedCreatureHasAtLeastEquipment;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "234")
@CardRegistration(set = "CMM", collectorNumber = "372")
@CardRegistration(set = "PIP", collectorNumber = "227")
@CardRegistration(set = "PIP", collectorNumber = "755")
public class BrassKnuckles extends Card {

    public BrassKnuckles() {
        addTrigger(Triggers.youCastThisSpell(), new CopyTriggeringSpellEffect(true));
        addStaticEffect(new ConditionalEffect(
                new EquippedCreatureHasAtLeastEquipment(2),
                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.EQUIPPED_CREATURE)));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
