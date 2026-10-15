package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WildHypothesis.class, Forest.class, GrizzlyBears.class})
class WildHypothesisTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Fractal with X counters, then surveils 2")
    void createsFractalAndSurveils() {
        Card topCardToGraveyard = new GrizzlyBears();
        Card topCardToKeep = new Forest();
        harness.setLibrary(player1, List.of(topCardToGraveyard, topCardToKeep));
        harness.setHand(player1, List.of(new WildHypothesis()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        Permanent fractal = findPermanent(player1, "Fractal");
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(fractal.getEffectivePower()).isEqualTo(2);
        assertThat(fractal.getEffectiveToughness()).isEqualTo(2);

        GameData localGd = harness.getGameData();
        harness.getGameService().handleInteractionAnswer(localGd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(localGd.playerDecks.get(player1.getId())).startsWith(topCardToKeep);
        assertThat(localGd.playerGraveyards.get(player1.getId())).contains(topCardToGraveyard);
    }

    @Test
    @DisplayName("An X=0 Fractal dies after surveil resolves")
    void zeroCountersFractalDiesAfterSurveil() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new WildHypothesis()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData localGd = harness.getGameData();
        harness.getGameService().handleInteractionAnswer(localGd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        harness.assertNotOnBattlefield(player1, "Fractal");
    }

    @Test
    @DisplayName("Surveil can keep both cards in a new order without touching the third card")
    void keepsAndReordersBothCards() {
        Card first = new Forest();
        Card second = new WildHypothesis();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new WildHypothesis()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second, third);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(findPermanent(player1, "Fractal").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Surveil can put both cards into the graveyard and leaves the third card on top")
    void putsBothCardsIntoGraveyard() {
        Card first = new Forest();
        Card second = new WildHypothesis();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new WildHypothesis()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second).doesNotContain(third);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(findPermanent(player1, "Fractal").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(3);
    }

    @Test
    @DisplayName("An empty library does not prevent creating the Fractal")
    void emptyLibraryStillCreatesFractal() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new WildHypothesis()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 4);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(findPermanent(player1, "Fractal").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(4);
        harness.assertInGraveyard(player1, "Wild Hypothesis");
        assertThat(gd.stack).isEmpty();
    }
}
