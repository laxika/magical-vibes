package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CrystalBarricade;
import com.github.laxika.magicalvibes.cards.g.GodsEyeGateToTheReikai;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrnateKanzashi.class, GodsEyeGateToTheReikai.class})
class OrnateKanzashiTest extends BaseCardTest {

    @Test
    @DisplayName("Activating exiles the opponent's top card and lets the controller play it this turn")
    void exilesOpponentTopCardAndGrantsPlayPermission() {
        Permanent kanzashi = addCreatureReady(player1, new OrnateKanzashi());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Card top = new GodsEyeGateToTheReikai();
        harness.setLibrary(player2, List.of(top, new GodsEyeGateToTheReikai()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(top.getId()));
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());
        assertThat(kanzashi.isTapped()).isTrue();

        gs.playCardFromExile(gd, player1, top.getId(), null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(top.getId()));
        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
    }

    @Test
    @DisplayName("Play permission expires at the end of the turn")
    void playPermissionExpiresAtEndOfTurn() {
        Permanent kanzashi = addCreatureReady(player1, new OrnateKanzashi());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Card top = new GodsEyeGateToTheReikai();
        harness.setLibrary(player2, List.of(top));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.exilePlayPermissions).containsEntry(top.getId(), player1.getId());
        assertThat(kanzashi.isTapped()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(top);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
    }

    @Test
    @DisplayName("Requires two generic mana to activate")
    void requiresTwoGenericMana() {
        Permanent kanzashi = addCreatureReady(player1, new OrnateKanzashi());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(kanzashi.isTapped()).isFalse();
    }

    @Test
    @CardUsed(CrystalBarricade.class)
    @DisplayName("Cannot activate against an opponent with hexproof")
    void cannotActivateAgainstHexproofOpponent() {
        addCreatureReady(player1, new OrnateKanzashi());
        harness.addToBattlefield(player2, new CrystalBarricade());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }
}
