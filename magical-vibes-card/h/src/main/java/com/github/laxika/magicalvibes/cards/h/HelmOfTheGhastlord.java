package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "SHM", collectorNumber = "166")
public class HelmOfTheGhastlord extends Card {

    public HelmOfTheGhastlord() {
        PermanentColorInPredicate blue = new PermanentColorInPredicate(Set.of(CardColor.BLUE));
        PermanentColorInPredicate black = new PermanentColorInPredicate(Set.of(CardColor.BLACK));
        target(TargetFilters.creature())
                // As long as enchanted creature is blue, it gets +1/+1 and has
                // "Whenever this creature deals damage to an opponent, draw a card."
                .addStaticEffect(new StaticBoostEffect(1, 1, Subjects.attachedCreature(), blue))
                .addStaticEffect(new GrantTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.dealsDamageToOpponent(),
                                new DrawCardEffect()), Subjects.attachedCreature(), blue))
                // As long as enchanted creature is black, it gets +1/+1 and has
                // "Whenever this creature deals damage to an opponent, that player discards a card."
                .addStaticEffect(new StaticBoostEffect(1, 1, Subjects.attachedCreature(), black))
                .addStaticEffect(new GrantTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.dealsDamageToOpponent(),
                        new DiscardEffect(1, Subjects.targetPlayer(), false)),
                        Subjects.attachedCreature(), black));
    }
}
