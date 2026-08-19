package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShroudedSerpentTest extends BaseCardTest {

    @Test
    @DisplayName("Defending player pays {4} and can block the Serpent")
    void defendingPlayerPaysToKeepSerpentBlockable() {
        addCreatureReady(player1, new ShroudedSerpent());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Declining to pay makes the Serpent unblockable")
    void decliningMakesSerpentUnblockable() {
        Permanent serpent = addCreatureReady(player1, new ShroudedSerpent());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(serpent.isCantBeBlocked()).isTrue();
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Accepting without enough mana makes the Serpent unblockable")
    void acceptingWithoutManaMakesSerpentUnblockable() {
        Permanent serpent = addCreatureReady(player1, new ShroudedSerpent());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(serpent.isCantBeBlocked()).isTrue();
    }
}
