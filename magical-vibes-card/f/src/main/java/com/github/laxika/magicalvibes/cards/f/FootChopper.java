package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.LivingWeaponEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMC", collectorNumber = "17")
public class FootChopper extends Card {

    public FootChopper() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new LivingWeaponEffect(new CreateTokenEffect("Ninja", 1, 1, CardColor.BLACK,
                        List.of(CardSubtype.NINJA), Set.of(), Set.of())));
        addStaticEffect(new GrantKeywordEffect(Keyword.FLYING, Subjects.attachedCreature()));
        // "Sacrifice it": the equipped creature that dealt the damage (the trigger event's creature); "its power" is its
        // last-known power.
        addTrigger(Triggers.equippedCreatureDealsCombatDamageToPlayer(),
                new MayEffect(SequenceEffect.of(
                        new SacrificeSelfEffect(Subjects.triggering()),
                        new ConditionalEffect(HappenedThisWay.sacrificed(),
                                new DrawCardEffect(AmountThisWay.sacrificedPower()))),
                        "You may sacrifice the equipped creature to draw cards equal to its power?"));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
