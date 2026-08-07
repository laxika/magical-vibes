package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TamiyoFieldResearcherTest extends BaseCardTest {

    @Test
    @DisplayName("+1: both chosen creatures dealing combat damage draw a card each")
    void plusOneDrawsForEachWatchedCreature() {
        addReadyTamiyo();
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Shock(), new GiantGrowth()));

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        int before = handSize(player1);
        declareAttackers(player1, List.of(1, 2));
        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(handSize(player1) - before).isEqualTo(2);
    }

    @Test
    @DisplayName("+1: a creature that was not chosen draws nothing")
    void plusOneIgnoresUnwatchedCreatures() {
        addReadyTamiyo();
        Permanent watched = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Shock(), new GiantGrowth()));

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(watched.getId()));
        harness.passBothPriorities();

        int before = handSize(player1);
        declareAttackers(player1, List.of(1, 2));
        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(handSize(player1) - before).isEqualTo(1);
    }

    @Test
    @DisplayName("+1: an opponent's chosen creature still draws for Tamiyo's controller")
    void plusOneDrawsForControllerWhenOpponentCreatureDealsDamage() {
        addReadyTamiyo();
        Permanent opposing = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Shock()));

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(opposing.getId()));
        harness.passBothPriorities();

        int before = handSize(player1);
        int opponentBefore = handSize(player2);
        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(handSize(player1) - before).isEqualTo(1);
        assertThat(handSize(player2)).isEqualTo(opponentBefore);
    }

    @Test
    @DisplayName("+1: the watch stops drawing once your next turn has begun")
    void plusOneWearsOffAtYourNextTurn() {
        addReadyTamiyo();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Shock(), new GiantGrowth()));

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bears.getId()));
        harness.passBothPriorities();

        endTurn(player1); // -> player2's turn, the watch is still active
        endTurn(player2); // -> player1's next turn, the watch expires here

        int before = handSize(player1);
        declareAttackers(player1, List.of(1));
        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(handSize(player1)).isEqualTo(before);
    }

    @Test
    @DisplayName("−2: taps two nonland permanents that then skip their next untap step")
    void minusTwoTapsAndLocksTwoPermanents() {
        Permanent tamiyo = addReadyTamiyo();
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(tamiyo.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();

        advanceToUpkeep(player2);

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
    }

    @Test
    @DisplayName("−2: a land is not a legal target")
    void minusTwoRejectsLandTarget() {
        addReadyTamiyo();
        harness.addToBattlefield(player2, new com.github.laxika.magicalvibes.cards.f.Forest());
        Permanent forest = findPermanent(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("−7: draws three cards and the emblem makes hand casts free")
    void minusSevenDrawsThreeAndGrantsFreeCastEmblem() {
        Permanent tamiyo = addReadyTamiyo();
        tamiyo.setCounterCount(CounterType.LOYALTY, 7);
        harness.setLibrary(player1, List.of(new Shock(), new GiantGrowth(), new GrizzlyBears()));

        int before = handSize(player1);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(handSize(player1) - before).isEqualTo(3);
        assertThat(gd.emblems).hasSize(1);

        // No mana is available, so the Grizzly Bears in hand can only be cast via the emblem.
        Card bears = gd.playerHands.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears")).findFirst().orElseThrow();
        int index = gd.playerHands.get(player1.getId()).indexOf(bears);
        harness.castCreature(player1, index);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    @DisplayName("−7: the emblem does not make an opponent's spells free")
    void minusSevenEmblemOnlyHelpsItsController() {
        Permanent tamiyo = addReadyTamiyo();
        tamiyo.setCounterCount(CounterType.LOYALTY, 7);
        harness.setLibrary(player1, List.of(new Shock(), new GiantGrowth(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private int handSize(Player player) {
        return gd.playerHands.get(player.getId()).size();
    }

    private Permanent addReadyTamiyo() {
        Permanent perm = new Permanent(new TamiyoFieldResearcher());
        perm.setCounterCount(CounterType.LOYALTY, 4);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    /**
     * Passes from {@code activePlayer}'s postcombat main through cleanup into the next player's turn.
     * Their hand is emptied first so cleanup never stops for a discard-to-hand-size choice.
     */
    private void endTurn(Player activePlayer) {
        harness.setHand(activePlayer, List.of());
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        for (int step = 0; step < 10 && activePlayer.getId().equals(gd.activePlayerId); step++) {
            harness.clearPriorityPassed();
            harness.passBothPriorities();
        }
    }
}
