package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToChosenPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "HOB", collectorNumber = "157")
public class GoblinPlateMail extends Card {

    public GoblinPlateMail() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new AmassGoblinsEffect(1),
                new AttachSourceEquipmentToChosenPermanentEffect());
        addStaticEffect(new StaticBoostEffect(1, 0, Set.of(Keyword.MENACE), GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{4}"));
    }
}
