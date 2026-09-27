package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.n.NinjaOfTheDeepHours;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HigureTheStillWind.class, GnarledMass.class, NinjaOfTheDeepHours.class})
class HigureTheStillWindTest extends BaseCardTest {

    private void giveNinjutsuMana() {
        harness.setHand(player1, List.of(new HigureTheStillWind()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Ninjutsu returns the unblocked attacker and puts Higure in tapped and attacking")
    void ninjutsuSwapsTheUnblockedAttacker() {
        Permanent bears = addCreatureReady(player1, new GnarledMass());
        addCreatureReady(player2, new GnarledMass());
        declareAttackersAndPrepareBlockers(List.of(0));
        giveNinjutsuMana();
        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Gnarled Mass");
        Permanent higure = findPermanent(player1, "Higure, the Still Wind");
        assertThat(higure.isTapped()).isTrue();
        assertThat(higure.isAttacking()).isTrue();
        assertThat(higure.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("A ninja put onto the battlefield attacking deals its combat damage")
    void ninjaDealsCombatDamage() {
        Permanent bears = addCreatureReady(player1, new GnarledMass());
        addCreatureReady(player2, new GnarledMass());
        declareAttackersAndPrepareBlockers(List.of(0));
        giveNinjutsuMana();
        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        resolveCombat();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Ninjutsu can't return a blocked attacker")
    void ninjutsuRejectsBlockedAttacker() {
        Permanent bears = addCreatureReady(player1, new GnarledMass());
        addCreatureReady(player2, new GnarledMass());
        declareAttackersAndPrepareBlockers(List.of(0));
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
        addCreatureReady(player1, new GnarledMass());
        Permanent theirs = addCreatureReady(player2, new GnarledMass());
        declareAttackersAndPrepareBlockers(List.of(0));
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
        harness.setLibrary(player1, List.of(new NinjaOfTheDeepHours(), new GnarledMass()));

        resolveCombat();
        if (!gd.interaction.isAwaitingInput()) {
            harness.passBothPriorities();
        }
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Ninja of the Deep Hours"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Accepting the combat-damage trigger puts the chosen Ninja into hand")
    void acceptingCombatDamageSearchPutsNinjaIntoHand() {
        Permanent higure = addCreatureReady(player1, new HigureTheStillWind());
        higure.setAttacking(true);
        Card ninja = new NinjaOfTheDeepHours();
        harness.setLibrary(player1, List.of(ninja, new GnarledMass()));

        resolveCombat();
        if (!gd.interaction.isAwaitingInput()) {
            harness.passBothPriorities();
        }
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().reveals()).isTrue();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(ninja);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the combat-damage trigger leaves the library untouched")
    void decliningCombatDamageSearchDoesNothing() {
        Permanent higure = addCreatureReady(player1, new HigureTheStillWind());
        higure.setAttacking(true);
        Card ninja = new NinjaOfTheDeepHours();
        Card nonNinja = new GnarledMass();
        harness.setLibrary(player1, List.of(ninja, nonNinja));
        List<Card> deck = gd.playerDecks.get(player1.getId());
        int handSize = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        if (!gd.interaction.isAwaitingInput()) {
            harness.passBothPriorities();
        }
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(deck).containsExactlyInAnyOrder(ninja, nonNinja);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("{2} makes a target Ninja unblockable, and a non-Ninja is an illegal target")
    void unblockableAbilityTargetsNinjasOnly() {
        Permanent higure = addCreatureReady(player1, new HigureTheStillWind());
        Permanent bears = addCreatureReady(player1, new GnarledMass());
        addCreatureReady(player2, new GnarledMass());
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

    @Test
    @DisplayName("{2} can target a Ninja an opponent controls")
    void unblockableAbilityCanTargetOpponentsNinja() {
        addCreatureReady(player1, new HigureTheStillWind());
        addCreatureReady(player1, new GnarledMass());
        Permanent opponentsNinja = addCreatureReady(player2, new HigureTheStillWind());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, opponentsNinja.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class);
    }
}
