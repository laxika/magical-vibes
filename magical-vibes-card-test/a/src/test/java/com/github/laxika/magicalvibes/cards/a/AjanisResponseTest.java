package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.h.HungryGraffalon;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AjanisResponse.class, HungryGraffalon.class})
class AjanisResponseTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {1}{W} when targeting a tapped creature")
    void reducedCostWhenTargetingTappedCreature() {
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player2, new HungryGraffalon());
        tappedCreature.tap();

        harness.setHand(player1, List.of(new AjanisResponse()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, tappedCreature.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertNotOnBattlefield(player2, "Hungry Graffalon");
        harness.assertInGraveyard(player2, "Hungry Graffalon");
    }

    @Test
    @DisplayName("Not playable with reduced-cost mana when only untapped creatures exist")
    void notPlayableWithReducedCostManaWhenNoTappedCreatures() {
        Permanent untappedCreature = harness.addToBattlefieldAndReturn(player2, new HungryGraffalon());

        harness.setHand(player1, List.of(new AjanisResponse()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, untappedCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Rejected cast (target does not qualify for reduction) returns the card to hand")
    void rejectedCastReturnsCardToHand() {
        // A tapped creature exists, so the playability check optimistically allows the cast
        // with the cost reduction, but the chosen target is untapped, so the reduction does
        // not apply and the cast is rejected mid-flight, after the card already left the hand.
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player2, new HungryGraffalon());
        tappedCreature.tap();
        Permanent untappedCreature = harness.addToBattlefieldAndReturn(player2, new HungryGraffalon());

        harness.setHand(player1, List.of(new AjanisResponse()));
        harness.addMana(player1, ManaColor.WHITE, 2); // covers only the reduced cost

        assertThatThrownBy(() -> harness.castInstant(player1, 0, untappedCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("target does not qualify");

        // The rejected cast returns the card to hand without consuming mana.
        GameData gd = harness.getGameData();
        harness.assertInHand(player1, "Ajani's Response");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Full cost works when targeting an untapped creature with enough mana")
    void fullCostUntappedWithEnoughMana() {
        Permanent untappedCreature = harness.addToBattlefieldAndReturn(player2, new HungryGraffalon());

        harness.setHand(player1, List.of(new AjanisResponse()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0, untappedCreature.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player2, "Hungry Graffalon");
    }

    @Test
    @DisplayName("Can target any creature, not only tapped ones")
    void canTargetUntappedCreatureWithFullMana() {
        Permanent untappedCreature = harness.addToBattlefieldAndReturn(player2, new HungryGraffalon());

        harness.setHand(player1, List.of(new AjanisResponse()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0, untappedCreature.getId());

        assertThat(harness.getGameData().stack).hasSize(1);
        assertThat(harness.getGameData().stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("Reduced cost applies when targeting opponent's tapped creature")
    void reducedCostForOpponentTappedCreature() {
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player2, new HungryGraffalon());
        tappedCreature.tap();

        harness.setHand(player1, List.of(new AjanisResponse()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, tappedCreature.getId());

        assertThat(harness.getGameData().stack).hasSize(1);
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot afford reduced cost when targeting untapped creature even if a tapped creature exists")
    void cannotUseReducedCostWithUntappedTarget() {
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player1, new HungryGraffalon());
        tappedCreature.tap();

        Permanent untappedCreature = harness.addToBattlefieldAndReturn(player2, new HungryGraffalon());

        harness.setHand(player1, List.of(new AjanisResponse()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, untappedCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cost reduction");
    }

    @Test
    @DisplayName("Reduced cost also applies to your own tapped creature")
    void destroysOwnTappedCreatureForReducedCost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HungryGraffalon());
        creature.tap();
        harness.setHand(player1, List.of(new AjanisResponse()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hungry Graffalon");
        harness.assertInGraveyard(player1, "Hungry Graffalon");
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Untapping the target after casting does not prevent destruction")
    void destroysTargetThatUntapsAfterCasting() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HungryGraffalon());
        creature.tap();
        harness.setHand(player1, List.of(new AjanisResponse()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, creature.getId());
        creature.untap();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hungry Graffalon");
        harness.assertInGraveyard(player2, "Hungry Graffalon");
        harness.assertInGraveyard(player1, "Ajani's Response");
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The reduction does not remove the white mana requirement")
    void reducedCostStillRequiresWhiteMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HungryGraffalon());
        creature.tap();
        harness.setHand(player1, List.of(new AjanisResponse()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Ajani's Response");
        harness.assertOnBattlefield(player2, "Hungry Graffalon");
        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("The reduced cost is used even when full-cost mana is available")
    void reducedCostLeavesExcessManaUnspent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HungryGraffalon());
        creature.tap();
        harness.setHand(player1, List.of(new AjanisResponse()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0, creature.getId());

        assertThat(harness.getGameData().stack).hasSize(1);
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Hungry Graffalon");
    }
}
