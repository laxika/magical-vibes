package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GemstoneCaverns.class, GrizzlyBears.class})
class GemstoneCavernsTest extends BaseCardTest {

    @Test
    void onlyTheNonStartingPlayerGetsThePregameChoice() {
        gd.startingPlayerId = player2.getId();
        harness.setHand(player1, List.of(new GemstoneCaverns(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        restartMulliganWithOpeningHands();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    void acceptingPregameChoicePlacesLuckCounterAndExilesAHandCard() {
        gd.startingPlayerId = player2.getId();
        harness.setHand(player1, List.of(new GemstoneCaverns(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        restartMulliganWithOpeningHands();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        harness.handleCardChosen(player1, 0);

        Permanent gemstone = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Gemstone Caverns"))
                .findFirst()
                .orElseThrow();
        assertThat(gemstone.getCounterCount(CounterType.LUCK)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    void acceptingPregameChoiceWorksWithNoOtherCardInHand() {
        harness.setHand(player1, List.of(new GemstoneCaverns()));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        restartMulliganWithOpeningHands();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Gemstone Caverns")
                        && permanent.getCounterCount(CounterType.LUCK) == 1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void withoutLuckCounterItProducesColorlessMana() {
        Permanent gemstone = harness.addToBattlefieldAndReturn(player1, new GemstoneCaverns());
        gemstone.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void withLuckCounterItProducesTheChosenColor() {
        Permanent gemstone = harness.addToBattlefieldAndReturn(player1, new GemstoneCaverns());
        gemstone.setSummoningSick(false);
        gemstone.setCounterCount(CounterType.LUCK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    private void restartMulliganWithOpeningHands() {
        gd.status = GameStatus.MULLIGAN;
        gd.playerKeptHand.clear();
        gd.playerNeedsToBottom.clear();
        gd.playerMulliganDecisionIds.clear();
        gd.playerBottomDecisionIds.clear();
        gd.pendingMayAbilities.clear();
        gd.pendingGemstoneCavernsChoice = null;
        gd.interaction.clearAwaitingInput();
        gd.startingPlayerId = player2.getId();
        gs.keepHand(gd, player2);
        gs.keepHand(gd, player1);
    }
}
