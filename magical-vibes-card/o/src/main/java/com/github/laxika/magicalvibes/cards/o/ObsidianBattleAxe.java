package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOR", collectorNumber = "144")
public class ObsidianBattleAxe extends Card {

    public ObsidianBattleAxe() {
        // Equipped creature gets +2/+1 and has haste.
        addStaticEffect(new StaticBoostEffect(2, 1, GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new GrantKeywordEffect(Keyword.HASTE, GrantScope.EQUIPPED_CREATURE));

        // Whenever a Warrior creature enters, you may attach this Equipment to it.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER),
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.WARRIOR),
                        new AttachSourceEquipmentToEnteringCreatureEffect()));

        // Equip {3}
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
