package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinCommando.class, GrizzlyBears.class, Forest.class})
class GoblinCommandoTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to a target creature")
    void dealsDamageToTargetCreature() {
        GrizzlyBears bear = new GrizzlyBears();
        bear.setPower(3);
        bear.setToughness(3);
        Permanent target = addCreatureReady(player2, bear);
        harness.setHand(player1, List.of(new GoblinCommando()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals lethal damage to a 2-toughness creature")
    void dealsLethalDamage() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GoblinCommando()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0, 0, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new GoblinCommando()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can deal damage to its controller's creature")
    void canDamageOwnCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GoblinCommando()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Goblin Commando");
    }

    @Test
    @DisplayName("Damage trigger resolves after Goblin Commando leaves")
    void triggerResolvesWithoutSource() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GoblinCommando()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        Permanent source = findPermanent(player1, "Goblin Commando");
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerHands.get(player1.getId()).add(source.getCard());
        resolveAllTriggers();

        harness.assertInHand(player1, "Goblin Commando");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A creature returning to the battlefield is a new target")
    void doesNotDamageReturnedTarget() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GoblinCommando()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).remove(target);
        Permanent returned = harness.addToBattlefieldAndReturn(player2, target.getCard());
        resolveAllTriggers();

        assertThat(returned.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("On an empty battlefield the mandatory trigger can target itself")
    void mustDamageItselfWhenOnlyCreature() {
        harness.setHand(player1, List.of(new GoblinCommando()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent source = findPermanent(player1, "Goblin Commando");
        harness.handlePermanentChosen(player1, source.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Goblin Commando");
        harness.assertInGraveyard(player1, "Goblin Commando");
    }
}
