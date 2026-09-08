package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Okk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrumpetingArmodonTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability makes the target block the Armodon")
    void resolvingAbilityAddsMustBlockRestriction() {
        Permanent armodon = addReadyArmodon(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMustBlockIds()).contains(armodon.getId());
    }

    @Test
    @DisplayName("Cannot activate the ability without enough mana")
    void cannotActivateWithoutMana() {
        addReadyArmodon(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Targeted creature must block the attacking Armodon")
    void targetedCreatureMustBlock() {
        Permanent armodon = addReadyArmodon(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        giveMana();

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        armodon.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("A targeted creature that cannot legally block is not required to block")
    void targetedOkkDoesNotHaveToBlockWithoutStrongerBlocker() {
        Permanent armodon = addReadyArmodon(player1);
        Permanent okk = addCreatureReady(player2, new Okk());
        giveMana();

        harness.activateAbility(player1, 0, null, okk.getId());
        harness.passBothPriorities();

        armodon.setAttacking(true);
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
        assertThat(okk.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Requirement lapses at end of turn")
    void restrictionResetsAtEndOfTurn() {
        Permanent armodon = addReadyArmodon(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        giveMana();

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();
        assertThat(blocker.getMustBlockIds()).contains(armodon.getId());

        blocker.resetModifiers();

        assertThat(blocker.getMustBlockIds()).isEmpty();
    }

    private void giveMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Permanent addReadyArmodon(Player player) {
        Permanent perm = new Permanent(new TrumpetingArmodon());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
