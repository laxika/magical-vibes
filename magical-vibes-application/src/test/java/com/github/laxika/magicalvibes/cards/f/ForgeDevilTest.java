package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.s.SanctuaryCat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForgeDevil.class, SanctuaryCat.class})
class ForgeDevilTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Forge Devil puts ETB triggered ability on the stack with the chosen target")
    void resolvingCreaturePutsEtbOnStack() {
        harness.addToBattlefield(player2, new SanctuaryCat());
        harness.setHand(player1, List.of(new ForgeDevil()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Sanctuary Cat");
        harness.castCreature(player1, 0, targetId);

        // Resolve the creature spell and queue its trigger.
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forge Devil");
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Forge Devil");
        assertThat(trigger.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("ETB deals 1 damage to target creature and 1 damage to you")
    void etbDeals1DamageToCreatureAnd1ToYou() {
        harness.addToBattlefield(player2, new SanctuaryCat());
        harness.setHand(player1, List.of(new ForgeDevil()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        UUID targetId = harness.getPermanentId(player2, "Sanctuary Cat");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities(); // Resolve creature
        harness.passBothPriorities(); // Resolve ETB

        assertThat(gd.stack).isEmpty();
        // You take 1 damage
        harness.assertLife(player1, 19);
        // Sanctuary Cat (1/2) takes 1 damage and survives
        Permanent cat = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(targetId))
                .findFirst().orElseThrow();
        assertThat(cat.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB kills a 1-toughness target creature")
    void etbKills1Toughness() {
        ForgeDevil smallCreature = new ForgeDevil();
        harness.addToBattlefield(player2, smallCreature);
        harness.setHand(player1, List.of(new ForgeDevil()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        UUID targetId = harness.getPermanentId(player2, "Forge Devil");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 19);
        harness.assertNotOnBattlefield(player2, "Forge Devil");
        harness.assertInGraveyard(player2, "Forge Devil");
    }

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution, but you still take no damage")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new SanctuaryCat());
        harness.setHand(player1, List.of(new ForgeDevil()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        UUID targetId = harness.getPermanentId(player2, "Sanctuary Cat");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities(); // Queue the trigger

        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities(); // The trigger fizzles

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("When entering alone, Forge Devil can target itself and damages its controller")
    void enteringAloneCanTargetItself() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent devil = harness.enterBattlefieldAndReturn(player1, new ForgeDevil());
        harness.handlePermanentChosen(player1, devil.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Forge Devil");
        harness.assertInGraveyard(player1, "Forge Devil");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing Forge Devil does not stop its triggered damage")
    void triggerResolvesAfterSourceLeaves() {
        Permanent cat = harness.addToBattlefieldAndReturn(player2, new SanctuaryCat());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ForgeDevil()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0, cat.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();
        assertThat(cat.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }
}
