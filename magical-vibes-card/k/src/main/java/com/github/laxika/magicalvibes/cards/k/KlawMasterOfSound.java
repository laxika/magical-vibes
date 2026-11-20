package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "46")
@CardRegistration(set = "MSC", collectorNumber = "352")
public class KlawMasterOfSound extends Card {

    public KlawMasterOfSound() {
        List<CardEffect> indestructible = List.of(
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, Subjects.source()));

        addTrigger(Triggers.youCastSpell().withStackEntryFilter(new StackEntryCastFromZonePredicate(Zone.EXILE)),
                indestructible.toArray(CardEffect[]::new));
        addTrigger(Triggers.youPlayLand().playedFrom(Zone.EXILE), indestructible.toArray(CardEffect[]::new));
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new ExileTopCardMayPlayThisTurnEffect(1, false).fromLibraryOf(Subjects.triggeringPlayer())
                        .exiledFaceDown().withAnyManaType().until(Duration.PERMANENT));
    }
}
