package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlabornMusketeer;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeaDrake.class, AlabornMusketeer.class, Island.class})
class SeaDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns two target lands you control to hand")
    void bouncesTwoOwnLands() {
        UUID firstLand = harness.addToBattlefieldAndReturn(player1, new Island()).getId();
        UUID secondLand = harness.addToBattlefieldAndReturn(player1, new Island()).getId();
        List<UUID> landIds = List.of(firstLand, secondLand);
        castSeaDrake(landIds);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertNotOnBattlefield(player1, "Island");
        assertThat(gd.playerHands.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Island")).count()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Sea Drake");
    }

    @Test
    @DisplayName("Cannot target lands you do not control")
    void cannotTargetOpponentLands() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        UUID ownLand = harness.getPermanentId(player1, "Island");
        UUID opponentLand = harness.getPermanentId(player2, "Island");

        assertThatThrownBy(() -> castSeaDrake(List.of(ownLand, opponentLand)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land you control");
    }

    @Test
    @DisplayName("Cannot target the same land twice")
    void cannotTargetSameLandTwice() {
        harness.addToBattlefield(player1, new Island());
        UUID land = harness.getPermanentId(player1, "Island");

        assertThatThrownBy(() -> castSeaDrake(List.of(land, land)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("All targets must be different");
    }

    @Test
    @DisplayName("Requires two targets")
    void requiresTwoTargets() {
        harness.addToBattlefield(player1, new Island());
        UUID land = harness.getPermanentId(player1, "Island");

        assertThatThrownBy(() -> castSeaDrake(List.of(land)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must choose 2 targets");
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonland() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new AlabornMusketeer());
        UUID land = harness.getPermanentId(player1, "Island");
        UUID creature = harness.getPermanentId(player1, "Alaborn Musketeer");

        assertThatThrownBy(() -> castSeaDrake(List.of(land, creature)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land you control");
    }

    @Test
    @DisplayName("Can enter without lands and without a target choice")
    void entersWithoutLands() {
        castSeaDrake(List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sea Drake");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.pendingInteractions).isEmpty();
    }

    @Test
    @DisplayName("With only one legal land the trigger is removed without choosing targets")
    void entersWithOnlyOneLand() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        castSeaDrake(List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sea Drake");
        harness.assertOnBattlefield(player1, "Island");
        harness.assertNotInHand(player1, "Island");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.pendingInteractions).isEmpty();
    }

    @Test
    @DisplayName("Targets can be chosen after Sea Drake enters")
    void choosesTargetsOnEntry() {
        UUID firstLand = harness.addToBattlefieldAndReturn(player1, new Island()).getId();
        UUID secondLand = harness.addToBattlefieldAndReturn(player1, new Island()).getId();
        castSeaDrake(List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, firstLand);
        harness.handlePermanentChosen(player1, secondLand);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sea Drake");
        harness.assertNotOnBattlefield(player1, "Island");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A land whose controller changes is not returned; the other land is returned")
    void returnsOnlyRemainingLegalTarget() {
        var firstLand = harness.addToBattlefieldAndReturn(player1, new Island());
        var secondLand = harness.addToBattlefieldAndReturn(player1, new Island());
        castSeaDrake(List.of(firstLand.getId(), secondLand.getId()));
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(firstLand);
        gd.playerBattlefields.get(player2.getId()).add(firstLand);
        gd.stolenCreatures.put(firstLand.getId(), player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(firstLand);
        harness.assertNotOnBattlefield(player1, "Island");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondLand.getCard());
        harness.assertOnBattlefield(player1, "Sea Drake");
    }

    @Test
    @DisplayName("When both targets become illegal no lands are returned and Sea Drake stays")
    void doesNothingWhenBothTargetsBecomeIllegal() {
        var firstLand = harness.addToBattlefieldAndReturn(player1, new Island());
        var secondLand = harness.addToBattlefieldAndReturn(player1, new Island());
        castSeaDrake(List.of(firstLand.getId(), secondLand.getId()));
        harness.passBothPriorities();

        for (var land : List.of(firstLand, secondLand)) {
            gd.playerBattlefields.get(player1.getId()).remove(land);
            gd.playerBattlefields.get(player2.getId()).add(land);
            gd.stolenCreatures.put(land.getId(), player1.getId());
        }
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(firstLand, secondLand);
        harness.assertNotInHand(player1, "Island");
        harness.assertNotInHand(player2, "Island");
        harness.assertOnBattlefield(player1, "Sea Drake");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A controlled land owned by the opponent returns to the opponent's hand")
    void returnsLandsToTheirOwners() {
        var borrowedLand = harness.addToBattlefieldAndReturn(player1, new Island());
        var ownLand = harness.addToBattlefieldAndReturn(player1, new Island());
        gd.stolenCreatures.put(borrowedLand.getId(), player2.getId());
        castSeaDrake(List.of(borrowedLand.getId(), ownLand.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Island");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ownLand.getCard());
        assertThat(gd.playerHands.get(player2.getId())).contains(borrowedLand.getCard());
    }

    @Test
    @DisplayName("The land-return trigger resolves even after Sea Drake leaves")
    void triggerSurvivesSourceLeaving() {
        UUID firstLand = harness.addToBattlefieldAndReturn(player1, new Island()).getId();
        UUID secondLand = harness.addToBattlefieldAndReturn(player1, new Island()).getId();
        castSeaDrake(List.of(firstLand, secondLand));
        harness.passBothPriorities();

        var drake = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(harness.getPermanentId(player1, "Sea Drake")))
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(drake);
        gd.playerGraveyards.get(player1.getId()).add(drake.getCard());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Island");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Sea Drake");
    }

    private void castSeaDrake(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new SeaDrake()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, targetIds);
    }
}
