package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MasterThief;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VedalkenShacklesTest extends BaseCardTest {

    @Test
    @DisplayName("Gains control of a creature whose power is at most the number of Islands you control")
    void gainsControlWithinIslandCount() {
        addIslands(player1, 2);
        Permanent shackles = addReadyShackles(player1);
        Permanent target = addReadyCreature(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, shackles), null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(shackles.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature whose power exceeds the number of Islands you control")
    void rejectsCreatureAboveIslandCount() {
        addIslands(player1, 2);
        Permanent shackles = addReadyShackles(player1);
        Permanent target = addReadyCreature(player2, new HillGiant());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, shackles), null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("number of Islands");
    }

    @Test
    @DisplayName("Losing Islands after resolution does not end control")
    void islandCountIsNotRecheckedAfterResolution() {
        addIslands(player1, 2);
        Permanent shackles = addReadyShackles(player1);
        Permanent target = addReadyCreature(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, shackles), null, target.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof Island);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    @DisplayName("Untapping Vedalken Shackles ends control")
    void untappingEndsControl() {
        addIslands(player1, 2);
        Permanent shackles = addReadyShackles(player1);
        Permanent target = addReadyCreature(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, shackles), null, target.getId());
        harness.passBothPriorities();
        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(shackles.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Control continues if another player gains control of the tapped Shackles")
    void sourceControllerChangeDoesNotEndControl() {
        addIslands(player1, 2);
        Permanent shackles = addReadyShackles(player1);
        Permanent target = addReadyCreature(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, shackles), null, target.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new MasterThief()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castCreature(player2, 0, 0, shackles.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(shackles);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    private Permanent addReadyShackles(Player player) {
        Permanent shackles = harness.addToBattlefieldAndReturn(player, new VedalkenShackles());
        shackles.setSummoningSick(false);
        return shackles;
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, card);
        creature.setSummoningSick(false);
        return creature;
    }

    private void addIslands(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Island());
        }
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean acceptUntap) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.handleMayAbilityChosen(newActivePlayer, acceptUntap);
    }
}
