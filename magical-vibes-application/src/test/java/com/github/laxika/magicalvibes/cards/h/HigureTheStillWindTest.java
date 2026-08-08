package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HigureTheStillWindTest extends BaseCardTest {

    private void giveNinjutsuMana() {
        harness.setHand(player1, List.of(new HigureTheStillWind()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Ninjutsu returns the unblocked attacker and puts Higure in tapped and attacking")
    void ninjutsuSwapsTheUnblockedAttacker() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        giveNinjutsuMana();
        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        Permanent higure = findPermanent(player1, "Higure, the Still Wind");
        assertThat(higure.isTapped()).isTrue();
        assertThat(higure.isAttacking()).isTrue();
        assertThat(higure.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("A ninja put onto the battlefield attacking deals its combat damage")
    void ninjaDealsCombatDamage() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        giveNinjutsuMana();
        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        resolveCombat();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Ninjutsu can't return a blocked attacker")
    void ninjutsuRejectsBlockedAttacker() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.clearPriorityPassed();
        giveNinjutsuMana();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unblocked attacker");
    }

    @Test
    @DisplayName("Ninjutsu can't return a creature an opponent controls")
    void ninjutsuRejectsOpponentAttacker() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent theirs = addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        giveNinjutsuMana();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, theirs.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unblocked attacker");
    }

    @Test
    @DisplayName("Combat damage to a player offers the Ninja tutor and finds only Ninja cards")
    void combatDamageOffersNinjaSearch() {
        Permanent higure = addCreatureReady(player1, new HigureTheStillWind());
        higure.setAttacking(true);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.add(new HigureTheStillWind());
        deck.add(new GrizzlyBears());

        resolveCombat();
        if (!gd.interaction.isAwaitingInput()) {
            harness.passBothPriorities();
        }
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Higure, the Still Wind"))
                .hasSize(1);
    }

    @Test
    @DisplayName("{2} makes a target Ninja unblockable, and a non-Ninja is an illegal target")
    void unblockableAbilityTargetsNinjasOnly() {
        Permanent higure = addCreatureReady(player1, new HigureTheStillWind());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.activateAbility(player1, 0, null, higure.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }
}
