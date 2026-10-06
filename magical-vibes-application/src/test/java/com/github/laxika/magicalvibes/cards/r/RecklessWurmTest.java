package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantDustwasp;
import com.github.laxika.magicalvibes.cards.p.PiracyCharm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RecklessWurm.class, PiracyCharm.class, GiantDustwasp.class})
class RecklessWurmTest extends BaseCardTest {

    private RecklessWurm discardViaPiracyCharm() {
        RecklessWurm wurm = new RecklessWurm();
        harness.setHand(player1, List.of(wurm));
        harness.setHand(player2, List.of(new PiracyCharm()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castModalInstant(player2, 0, 2, List.of(player1.getId()));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        return wurm;
    }

    @Test
    @DisplayName("Discarding Reckless Wurm exiles it and offers madness cast")
    void discardTriggersMadness() {
        RecklessWurm wurm = discardViaPiracyCharm();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(wurm.getId()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Declining madness puts Reckless Wurm into the graveyard")
    void decliningMadnessGoesToGraveyard() {
        RecklessWurm wurm = discardViaPiracyCharm();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(wurm.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(wurm.getId()));
    }

    @Test
    @DisplayName("Accepting madness cast pays {2}{R} and puts Reckless Wurm onto the battlefield")
    void acceptingMadnessCastsCreature() {
        RecklessWurm wurm = discardViaPiracyCharm();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(wurm.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Madness cannot be paid with only generic mana")
    void madnessRequiresRedMana() {
        RecklessWurm wurm = discardViaPiracyCharm();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(wurm.getId()));
        harness.assertInGraveyard(player1, "Reckless Wurm");
        harness.assertNotOnBattlefield(player1, "Reckless Wurm");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    @DisplayName("Madness cannot be cast when the generic portion cannot be paid")
    void madnessRequiresFullCost() {
        discardViaPiracyCharm();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Reckless Wurm");
        harness.assertNotOnBattlefield(player1, "Reckless Wurm");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Madness remains optional when its cost can be paid")
    void decliningAffordableMadnessDoesNotSpendMana() {
        discardViaPiracyCharm();
        harness.addMana(player1, ManaColor.RED, 3);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Reckless Wurm");
        harness.assertNotOnBattlefield(player1, "Reckless Wurm");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    @DisplayName("Trample assigns excess combat damage to the defending player")
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new RecklessWurm());
        Permanent blocker = addCreatureReady(player2, new GiantDustwasp());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 3,
                player2.getId(), 1
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }
}
