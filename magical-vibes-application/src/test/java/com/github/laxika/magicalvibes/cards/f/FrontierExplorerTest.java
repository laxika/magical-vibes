package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AzusaLostButSeeking;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FrontierExplorer.class, Plains.class, Forest.class, AzusaLostButSeeking.class, FireLordZuko.class})
class FrontierExplorerTest extends BaseCardTest {

    @Test
    @DisplayName("Allows a basic Plains, but not another sideboard card, to be played this turn")
    void allowsOnlyBasicPlainsFromOutsideTheGame() {
        Card plains = new Plains();
        Card forest = new Forest();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(plains, forest)));
        addCreatureReady(player1, new FrontierExplorer());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.outsideGamePlayPermissions).contains(plains.getId());
        assertThat(gd.outsideGamePlayPermissions).doesNotContain(forest.getId());

        prepareMainPhase();
        harness.castFromExile(player1, plains.getId());
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(forest);
        assertThat(gd.landsPlayedThisTurn).containsEntry(player1.getId(), 1);

        prepareMainPhase();
        assertThatThrownBy(() -> harness.castFromExile(player1, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("permission");
    }

    @Test
    @DisplayName("Expires the outside-game Plains permission at end of turn")
    void permissionExpiresAtEndOfTurn() {
        Card plains = new Plains();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(plains)));
        addCreatureReady(player1, new FrontierExplorer());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.outsideGamePlayPermissions).contains(plains.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.outsideGamePlayPermissions).doesNotContain(plains.getId());
    }

    @Test
    @DisplayName("One activation permits only one outside-game Plains even with extra land plays")
    void oneActivationAllowsOnlyOnePlains() {
        Card first = new Plains();
        Card second = new Plains();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(first, second)));
        addCreatureReady(player1, new FrontierExplorer());
        addCreatureReady(player1, new AzusaLostButSeeking());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        prepareMainPhase();
        harness.castFromExile(player1, first.getId());

        prepareMainPhase();
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(second);
        assertThat(gd.landsPlayedThisTurn).containsEntry(player1.getId(), 1);
    }

    @Test
    @DisplayName("Two activations independently permit two outside-game Plains")
    void twoActivationsAllowTwoPlains() {
        Card first = new Plains();
        Card second = new Plains();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(first, second)));
        addCreatureReady(player1, new FrontierExplorer());
        addCreatureReady(player1, new FrontierExplorer());
        addCreatureReady(player1, new AzusaLostButSeeking());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        prepareMainPhase();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        prepareMainPhase();
        harness.castFromExile(player1, first.getId());
        prepareMainPhase();
        harness.castFromExile(player1, second.getId());

        assertThat(gd.playerSideboards.get(player1.getId())).isEmpty();
        assertThat(gd.landsPlayedThisTurn).containsEntry(player1.getId(), 2);
    }

    @Test
    @DisplayName("Outside-game permission does not grant an additional land play")
    void normalLandPlayLimitStillApplies() {
        Card plains = new Plains();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(plains)));
        addCreatureReady(player1, new FrontierExplorer());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        prepareMainPhase();
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        prepareMainPhase();
        assertThatThrownBy(() -> harness.castFromExile(player1, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(plains);
        assertThat(gd.landsPlayedThisTurn).containsEntry(player1.getId(), 1);
    }

    @Test
    @DisplayName("A Plains played from outside the game does not trigger entry from exile")
    void outsideGameLandDoesNotEnterFromExile() {
        Card plains = new Plains();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(plains)));
        var explorer = addCreatureReady(player1, new FrontierExplorer());
        var zuko = addCreatureReady(player1, new FireLordZuko());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        prepareMainPhase();
        harness.castFromExile(player1, plains.getId());
        harness.passBothPriorities();

        assertThat(explorer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(zuko.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
