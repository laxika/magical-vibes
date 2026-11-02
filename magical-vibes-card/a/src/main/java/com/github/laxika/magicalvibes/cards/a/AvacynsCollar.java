package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DKA", collectorNumber = "145")
@CardRegistration(set = "SIS", collectorNumber = "68")
public class AvacynsCollar extends Card {

    public AvacynsCollar() {
        // Equipped creature gets +1/+0 and has vigilance
        addStaticEffect(new StaticBoostEffect(1, 0, GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.EQUIPPED_CREATURE));

        // Whenever equipped creature dies, if it was a Human, create a 1/1 white Spirit creature token with flying
        addTrigger(Triggers.creatureDies(new PermanentHasSubtypePredicate(CardSubtype.HUMAN), ObjectRelation.EQUIPPED),
                CreateTokenEffect.whiteSpirit(1));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
