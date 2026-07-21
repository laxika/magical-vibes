package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DemonicDreadTest extends BaseCardTest {

    // ===== Target creature can't block this turn =====

    @Test
    @DisplayName("Target creature can't block this turn")
    void targetCreatureCantBlock() {
        prepareCaster();
        Permanent blocker = addReadyCreature(player2);

        harness.castSorcery(player1, 0, List.of(blocker.getId()));
        harness.passBothPriorities(); // resolve cascade (empty library, no-op)
        harness.passBothPriorities(); // resolve the spell

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Targeted creature actually cannot be declared as a blocker")
    void targetedCreatureCannotBlock() {
        prepareCaster();
        Permanent attacker = addReadyCreature(player1);
        Permanent blocker = addReadyCreature(player2);

        harness.castSorcery(player1, 0, List.of(blocker.getId()));
        harness.passBothPriorities(); // resolve cascade (empty library, no-op)
        harness.passBothPriorities(); // resolve the spell

        assertThat(blocker.isCantBlockThisTurn()).isTrue();

        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Cascade =====

    @Test
    @DisplayName("Cascade digs past a land to the first lesser-mana-value nonland")
    void cascadeOffersLesserManaValueNonland() {
        prepareCaster();
        Permanent blocker = addReadyCreature(player2);

        // Demonic Dread is {1}{B}{R} = mana value 3. Dig skips the Mountain and stops at
        // Grizzly Bears (MV 2 < 3).
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new Mountain(), new GrizzlyBears()));

        harness.castSorcery(player1, 0, List.of(blocker.getId()));
        harness.passBothPriorities(); // resolve the cascade trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        List<String> castable = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().stream().map(Card::getName).toList();
        assertThat(castable).containsExactly("Grizzly Bears");
    }

    // ===== Illegal target =====

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        prepareCaster();
        addReadyCreature(player2); // valid target so the spell is playable

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private void prepareCaster() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        // Empty library by default so the cascade trigger is a no-op unless a test stocks it.
        gd.playerDecks.get(player1.getId()).clear();
        harness.setHand(player1, List.of(new DemonicDread()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private Permanent addReadyCreature(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
