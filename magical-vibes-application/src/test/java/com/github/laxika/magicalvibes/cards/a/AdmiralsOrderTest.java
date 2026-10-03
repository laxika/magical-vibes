package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AdmiralsOrder.class, GrizzlyBears.class, Shock.class})
class AdmiralsOrderTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a target spell when cast for its mana cost")
    void countersTargetSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new AdmiralsOrder()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Admiral's Order");
    }

    @Test
    @DisplayName("Can be cast for {U} after attacking this turn")
    void castsForAlternateCostAfterAttacking() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AdmiralsOrder()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.addToBattlefield(player2, new GrizzlyBears());
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());
        harness.castWithAlternateCost(player1, 0, shock.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertInGraveyard(player1, "Admiral's Order");
    }

    @Test
    @DisplayName("The alternate cost requires having attacked this turn")
    void alternateCostRequiresRaid() {
        harness.setHand(player1, List.of(new AdmiralsOrder()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can counter its controller's own spell")
    void countersOwnSpell() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock, new AdmiralsOrder()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, shock.getId());

        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player1, "Admiral's Order");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An opponent's attack does not enable the alternate cost")
    void opponentsAttackDoesNotEnableRaid() {
        addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AdmiralsOrder()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        harness.castInstant(player2, 0, player1.getId());

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Admiral's Order");
    }

    @Test
    @DisplayName("Raid remains available after the attacker dies and combat ends")
    void raidRemainsAvailableAfterAttackerDies() {
        var attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AdmiralsOrder()));
        Shock firstShock = new Shock();
        Shock secondShock = new Shock();
        harness.setHand(player2, List.of(firstShock, secondShock));
        harness.addMana(player2, ManaColor.RED, 1);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        harness.castAndResolveInstant(player2, 0, attacker.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.castWithAlternateCost(player1, 0, secondShock.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertInGraveyard(player1, "Admiral's Order");
        harness.assertLife(player1, 20);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(firstShock, secondShock);
    }
}
