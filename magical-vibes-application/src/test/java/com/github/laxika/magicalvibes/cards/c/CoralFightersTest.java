package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CoralFightersTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent atk = new Permanent(new CoralFighters());
        atk.setSummoningSick(false);
        atk.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atk);
        return atk;
    }

    private void setDefenderLibrary() {
        harness.setLibrary(player2, new ArrayList<>(List.of(new GrizzlyBears(), new HillGiant(), new Forest())));
    }

    private void attackUnblocked() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        // Advance into the declare-blockers step (the defender has no blockers), firing the
        // "attacks and isn't blocked" trigger, then resolve it to present the may choice.
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private List<String> libraryNames() {
        return gd.playerDecks.get(player2.getId()).stream().map(Card::getName).toList();
    }

    @Test
    @DisplayName("Accepting the may puts the looked-at top card on the bottom of the defender's library")
    void unblockedAcceptBottomsTopCard() {
        setDefenderLibrary();
        addAttacker();

        attackUnblocked();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        // Accepting puts the ability on the stack; let it resolve.
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(libraryNames()).containsExactly("Hill Giant", "Forest", "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the may leaves the defender's library untouched")
    void unblockedDeclineLeavesLibraryUntouched() {
        setDefenderLibrary();
        addAttacker();

        attackUnblocked();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(libraryNames()).containsExactly("Grizzly Bears", "Hill Giant", "Forest");
    }

    @Test
    @DisplayName("Blocked attacker does not trigger the ability")
    void blockedNoTrigger() {
        setDefenderLibrary();

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        addAttacker();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(libraryNames()).containsExactly("Grizzly Bears", "Hill Giant", "Forest");
    }

    @Test
    @DisplayName("Empty defender library presents no choice")
    void emptyLibraryNoChoice() {
        harness.setLibrary(player2, new ArrayList<>());
        addAttacker();

        attackUnblocked();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
}
