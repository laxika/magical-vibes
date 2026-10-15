package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SelkieHedgeMage.class, Forest.class, Island.class, GrizzlyBears.class})
class SelkieHedgeMageTest extends BaseCardTest {


    @Test
    @DisplayName("With two Forests, ETB may gain 3 life")
    void forestGateGainsLife() {
        addLands(player1, 2, 0);
        castSelkie();
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ConditionalEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Declining the Forest trigger gains no life")
    void forestGateDeclinedGainsNoLife() {
        addLands(player1, 2, 0);
        castSelkie();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("With only one Forest the life-gain trigger does not fire")
    void oneForestDoesNotTrigger() {
        addLands(player1, 1, 0);
        castSelkie();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }


    @Test
    @DisplayName("With two Islands, ETB may return a tapped creature to its owner's hand")
    void islandGateBouncesTappedCreature() {
        addLands(player1, 0, 2);
        Permanent bears = addBears(player2);
        bears.tap();
        castSelkie();
        harness.passBothPriorities(); // resolve creature spell -> trigger-time target prompt
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities(); // resolve ConditionalEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(bears.getId()));
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the Island trigger leaves the tapped creature on the battlefield")
    void islandGateDeclinedLeavesCreature() {
        addLands(player1, 0, 2);
        Permanent bears = addBears(player2);
        bears.tap();
        castSelkie();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("With two Islands but no tapped creature, the bounce trigger finds no legal target")
    void islandGateNoTappedCreatureNoTrigger() {
        addLands(player1, 0, 2);
        Permanent bears = addBears(player2); // left untapped
        castSelkie();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("With only one Island the bounce trigger does not fire")
    void oneIslandDoesNotTrigger() {
        addLands(player1, 0, 1);
        Permanent bears = addBears(player2);
        bears.tap();
        castSelkie();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(bears.getId()));
    }


    @Test
    @DisplayName("With no Forests or Islands, neither ability triggers")
    void neitherGateTriggers() {
        castSelkie();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }


    @Test
    @DisplayName("Both land conditions create separate triggered abilities")
    void bothConditionsCreateSeparateTriggers() {
        addLands(player1, 2, 2);
        Permanent bears = addBears(player2);
        bears.tap();
        castSelkie();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("The Forest condition is checked again at resolution")
    void losingForestBeforeResolutionPreventsLifeGain() {
        addLands(player1, 2, 0);
        castSelkie();
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).removeFirst();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The Island condition is checked again at resolution")
    void losingIslandBeforeResolutionPreventsBounce() {
        addLands(player1, 0, 2);
        Permanent bears = addBears(player2);
        bears.tap();
        castSelkie();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        gd.playerBattlefields.get(player1.getId()).removeFirst();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A creature untapped in response is no longer a legal bounce target")
    void untappedTargetIsNotReturned() {
        addLands(player1, 0, 2);
        Permanent bears = addBears(player2);
        bears.tap();
        castSelkie();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        bears.untap();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
    @Test
    @DisplayName("No legal bounce target does not suppress the separate life-gain ability")
    void bothConditionsWithoutTappedCreatureStillGainLife() {
        addLands(player1, 2, 2);
        castSelkie();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 23);
    }
    private void castSelkie() {
        harness.setHand(player1, List.of(new SelkieHedgeMage()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
    }

    private void addLands(Player player, int forests, int islands) {
        for (int i = 0; i < forests; i++) {
            harness.addToBattlefield(player, new Forest());
        }
        for (int i = 0; i < islands; i++) {
            harness.addToBattlefield(player, new Island());
        }
    }

    private Permanent addBears(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }
}
