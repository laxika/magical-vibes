package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({FrontierExplorer.class, Plains.class, Forest.class})
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

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
