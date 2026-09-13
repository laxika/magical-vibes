package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.c.Compost;
import com.github.laxika.magicalvibes.cards.d.Donate;
import com.github.laxika.magicalvibes.cards.g.GoliathBeetle;
import com.github.laxika.magicalvibes.cards.y.YavimayaHollow;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Rescue.class, GoliathBeetle.class, Compost.class, YavimayaHollow.class, Donate.class})
class RescueTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Rescue puts it on the stack with target")
    void castingPutsOnStack() {
        harness.addToBattlefield(player1, new GoliathBeetle());
        harness.setHand(player1, List.of(new Rescue()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player1, "Goliath Beetle");
        harness.castInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    // ===== Target validation =====

    @Test
    @DisplayName("Cannot target opponent's creature")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new GoliathBeetle()); // valid target so spell is playable
        harness.addToBattlefield(player2, new GoliathBeetle());
        harness.setHand(player1, List.of(new Rescue()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Goliath Beetle");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a permanent you control");
    }

    // ===== Resolving: bounce own creature =====

    @Test
    @DisplayName("Resolving returns own creature to hand")
    void resolvingReturnsOwnCreatureToHand() {
        harness.addToBattlefield(player1, new GoliathBeetle());
        harness.setHand(player1, List.of(new Rescue()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player1, "Goliath Beetle");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Goliath Beetle");
        harness.assertInHand(player1, "Goliath Beetle");
    }

    // ===== Resolving: bounce own enchantment =====

    @Test
    @DisplayName("Resolving returns own enchantment to hand")
    void resolvingReturnsOwnEnchantmentToHand() {
        harness.addToBattlefield(player1, new Compost());
        harness.setHand(player1, List.of(new Rescue()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player1, "Compost");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Compost");
        harness.assertInHand(player1, "Compost");
    }

    // ===== Resolving: bounce own land =====

    @Test
    @DisplayName("Resolving returns own land to hand")
    void resolvingReturnsOwnLandToHand() {
        harness.addToBattlefield(player1, new YavimayaHollow());
        harness.setHand(player1, List.of(new Rescue()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player1, "Yavimaya Hollow");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Yavimaya Hollow");
        harness.assertInHand(player1, "Yavimaya Hollow");
    }

    // ===== Rescue goes to graveyard =====

    @Test
    @DisplayName("Rescue goes to graveyard after resolving")
    void rescueGoesToGraveyardAfterResolving() {
        harness.addToBattlefield(player1, new GoliathBeetle());
        harness.setHand(player1, List.of(new Rescue()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player1, "Goliath Beetle");
        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Rescue");
    }

    @Test
    @DisplayName("Returns a controlled permanent to its owner's hand")
    void returnsControlledPermanentToOwnersHand() {
        var donatedPermanent = harness.addToBattlefieldAndReturn(player1, new GoliathBeetle());
        harness.setHand(player1, List.of(new Donate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId(), donatedPermanent.getId()));

        harness.setHand(player2, List.of(new Rescue()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, donatedPermanent.getId());

        harness.assertNotOnBattlefield(player2, "Goliath Beetle");
        harness.assertInHand(player1, "Goliath Beetle");
        harness.assertNotInHand(player2, "Goliath Beetle");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new GoliathBeetle());
        harness.setHand(player1, List.of(new Rescue()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player1, "Goliath Beetle");
        harness.castInstant(player1, 0, targetId);

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getId().equals(targetId));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Rescue");
    }
}
