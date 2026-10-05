package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.s.SunhomeFortressOfTheLegion;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OnTheTrail.class, SunhomeFortressOfTheLegion.class, SakuraTribeElder.class})
class OnTheTrailTest extends BaseCardTest {

    @Test
    @DisplayName("May put a land from hand onto the battlefield tapped after the second draw")
    void putsLandTappedAfterSecondDraw() {
        harness.addToBattlefield(player1, new OnTheTrail());
        SunhomeFortressOfTheLegion land = new SunhomeFortressOfTheLegion();
        harness.setHand(player1, List.of(land));
        setLibrary(player1, new SakuraTribeElder(), new SakuraTribeElder());

        drawCard(player1);
        assertThat(gd.stack).isEmpty();

        drawCard(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card == land)
                .hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.getCard() == land && permanent.isTapped());
    }

    @Test
    @DisplayName("Declining the trigger leaves the land in hand")
    void decliningTriggerLeavesLandInHand() {
        harness.addToBattlefield(player1, new OnTheTrail());
        SunhomeFortressOfTheLegion land = new SunhomeFortressOfTheLegion();
        harness.setHand(player1, List.of(land));
        setLibrary(player1, new SakuraTribeElder(), new SakuraTribeElder());

        drawCard(player1);
        drawCard(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(land).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(permanent -> permanent.getCard() == land);
    }

    @Test
    @DisplayName("Triggers only on the second card drawn each turn")
    void triggersOnlyOnSecondCardDraw() {
        harness.addToBattlefield(player1, new OnTheTrail());
        harness.setHand(player1, List.of(new SunhomeFortressOfTheLegion()));
        setLibrary(player1, new SakuraTribeElder(), new SakuraTribeElder(), new SakuraTribeElder());

        drawCard(player1);
        drawCard(player1);
        assertThat(gd.stack).hasSize(1);
        drawCard(player1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The controller's second draw also triggers during an opponent's turn")
    void triggersDuringOpponentsTurn() {
        gd.activePlayerId = player2.getId();
        harness.addToBattlefield(player1, new OnTheTrail());
        setLibrary(player1, new SakuraTribeElder(), new SakuraTribeElder());

        drawCard(player1);
        assertThat(gd.stack).isEmpty();
        drawCard(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's second draw does not trigger the enchantment")
    void opponentsDrawsDoNotTrigger() {
        harness.addToBattlefield(player1, new OnTheTrail());
        setLibrary(player2, new SakuraTribeElder(), new SakuraTribeElder());

        drawCard(player2);
        drawCard(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A first draw before the enchantment enters still counts")
    void countsDrawBeforeEntering() {
        setLibrary(player1, new SakuraTribeElder(), new SakuraTribeElder());
        drawCard(player1);
        harness.addToBattlefield(player1, new OnTheTrail());

        drawCard(player1);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Accepting without a land in hand puts no creature onto the battlefield")
    void acceptingWithoutLandDoesNothing() {
        harness.addToBattlefield(player1, new OnTheTrail());
        harness.setHand(player1, List.of());
        setLibrary(player1, new SakuraTribeElder(), new SakuraTribeElder());
        drawCard(player1);
        drawCard(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
    @Test
    @DisplayName("The land drawn as the second card can be put onto the battlefield")
    void canPutSecondDrawnCardOntoBattlefield() {
        harness.addToBattlefield(player1, new OnTheTrail());
        harness.setHand(player1, List.of());
        SunhomeFortressOfTheLegion land = new SunhomeFortressOfTheLegion();
        setLibrary(player1, new SakuraTribeElder(), land);

        drawCard(player1);
        drawCard(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(land).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.getCard() == land && permanent.isTapped());
    }
    private void drawCard(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }

    private void setLibrary(Player player, Card... cards) {
        harness.setLibrary(player, List.of(cards));
    }
}
