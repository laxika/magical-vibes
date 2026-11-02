package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOI", collectorNumber = "264")
@CardRegistration(set = "SIR", collectorNumber = "257")
public class SlayersPlate extends Card {

    public SlayersPlate() {
        addStaticEffect(new StaticBoostEffect(4, 2, GrantScope.EQUIPPED_CREATURE));
        addTrigger(Triggers.creatureDies(new PermanentHasSubtypePredicate(CardSubtype.HUMAN), ObjectRelation.EQUIPPED),
                CreateTokenEffect.whiteSpirit(1));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
