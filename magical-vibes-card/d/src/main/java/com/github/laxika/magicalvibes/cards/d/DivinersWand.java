package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "142")
public class DivinersWand extends Card {

    public DivinersWand() {
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.youDrawCard(),
                SequenceEffect.of(new BoostSelfEffect(1, 1),
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF))),
                GrantScope.EQUIPPED_CREATURE));

        // Equipped creature has "{4}: Draw a card."
        addStaticEffect(new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        false,
                        "{4}",
                        List.of(new DrawCardEffect(1)),
                        "{4}: Draw a card."
                ),
                GrantScope.EQUIPPED_CREATURE
        ));

        // Whenever a Wizard creature enters, you may attach this Equipment to it.
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasSubtypePredicate(CardSubtype.WIZARD))),
                        ObjectRelation.ANOTHER),
                new MayEffect(AttachSourceEquipmentToTargetCreatureEffect.forTriggeringCreature(),
                        "Attach this Equipment to it?"));

        // Equip {3}
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
