package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TimeSidewalk.class)
class TimeSidewalkTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Time Sidewalk gives its controller an extra turn")
    void castingGivesControllerAnExtraTurn() {
        TimeSidewalk timeSidewalk = new TimeSidewalk();
        harness.castFromHand(player1, timeSidewalk, "{4}{U}{U}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
        harness.assertInGraveyard(player1, "Time Sidewalk");
    }

    @Test
    @DisplayName("Accepting the opening-hand ability shuffles four Time Walk token cards into the library")
    void openingHandAbilityCreatesTimeWalkTokenCards() {
        GameTestHarness openingHarness = new GameTestHarness();
        var openingPlayer = openingHarness.getPlayer1();
        TimeSidewalk timeSidewalk = new TimeSidewalk();
        openingHarness.setHand(openingPlayer, List.of(timeSidewalk));
        openingHarness.setLibrary(openingPlayer, List.of());
        openingHarness.skipMulligan();

        openingHarness.passBothPriorities();
        openingHarness.handleMayAbilityChosen(openingPlayer, true);

        GameData openingGame = openingHarness.getGameData();
        assertThat(openingGame.playerHands.get(openingPlayer.getId())).doesNotContain(timeSidewalk);
        assertThat(openingGame.getPlayerExiledCards(openingPlayer.getId())).contains(timeSidewalk);
        assertThat(openingGame.playerDecks.get(openingPlayer.getId())).hasSize(4);
        assertThat(openingGame.playerDecks.get(openingPlayer.getId()))
                .allSatisfy(TimeSidewalkTest::assertTimeWalkTokenCard);
    }

    @Test
    @DisplayName("Declining the opening-hand ability leaves Time Sidewalk in hand")
    void decliningOpeningHandAbilityLeavesCardInHand() {
        GameTestHarness openingHarness = new GameTestHarness();
        var openingPlayer = openingHarness.getPlayer1();
        TimeSidewalk timeSidewalk = new TimeSidewalk();
        openingHarness.setHand(openingPlayer, List.of(timeSidewalk));
        openingHarness.setLibrary(openingPlayer, List.of());
        openingHarness.skipMulligan();

        openingHarness.passBothPriorities();
        openingHarness.handleMayAbilityChosen(openingPlayer, false);

        GameData openingGame = openingHarness.getGameData();
        assertThat(openingGame.playerHands.get(openingPlayer.getId())).containsExactly(timeSidewalk);
        assertThat(openingGame.getPlayerExiledCards(openingPlayer.getId())).doesNotContain(timeSidewalk);
        assertThat(openingGame.playerDecks.get(openingPlayer.getId())).isEmpty();
    }

    private static void assertTimeWalkTokenCard(Card card) {
        assertThat(card.getName()).isEqualTo("Time Walk");
        assertThat(card.getType()).isEqualTo(CardType.SORCERY);
        assertThat(card.getManaCost()).isEqualTo("{1}{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.isToken()).isTrue();
        assertThat(card.isTokenCard()).isTrue();
        List<CardEffect> effects = card.getEffects(EffectSlot.SPELL);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(ControllerExtraTurnEffect.class);
    }
}
