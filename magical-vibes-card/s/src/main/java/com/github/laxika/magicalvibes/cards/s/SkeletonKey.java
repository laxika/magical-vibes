package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "SOI", collectorNumber = "263")
public class SkeletonKey extends Card {

    public SkeletonKey() {
        // Equipped creature has skulk.
        addStaticEffect(new GrantKeywordEffect(Keyword.SKULK, Subjects.attachedCreature()));

        // Whenever equipped creature deals combat damage to a player, you may draw a card. If you
        // do, discard a card.
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayEffect(
                        SequenceEffect.of(
                                new DrawCardEffect(),
                                new DiscardEffect(1, Subjects.you())),
                        "Draw a card?"));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
