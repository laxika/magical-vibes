package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.s.SwiftfootBoots;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnimalFriend.class, GrizzlyBears.class, HolyStrength.class, SwiftfootBoots.class})
class AnimalFriendTest extends BaseCardTest {

    @Test
    void createsSquirrelWithOneCounterPerOtherAttachment() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        attach(player1, new AnimalFriend(), bear);
        attach(player1, new HolyStrength(), bear);
        attach(player1, new SwiftfootBoots(), bear);

        attackWith(player1, bear);

        Permanent squirrel = findPermanent(player1, "Squirrel");
        assertThat(squirrel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, squirrel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, squirrel)).isEqualTo(3);
    }

    @Test
    void doesNotCountAnimalFriendItself() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        attach(player1, new AnimalFriend(), bear);

        attackWith(player1, bear);

        Permanent squirrel = findPermanent(player1, "Squirrel");
        assertThat(squirrel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void tokenIsControlledByTheAttackingCreatureController() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        attach(player1, new AnimalFriend(), bear);
        attach(player2, new HolyStrength(), bear);

        attackWith(player2, bear);

        assertThat(findPermanents(player2, "Squirrel")).hasSize(1);
        assertThat(findPermanents(player1, "Squirrel")).isEmpty();
        assertThat(findPermanent(player2, "Squirrel")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void attackWith(Player player, Permanent creature) {
        declareAttackers(player, List.of(gd.playerBattlefields.get(player.getId()).indexOf(creature)));
        resolveAllTriggers();
    }

    private Permanent attach(Player controller, Card card, Permanent creature) {
        Permanent attachment = harness.addToBattlefieldAndReturn(controller, card);
        attachment.setAttachedTo(creature.getId());
        return attachment;
    }
}
