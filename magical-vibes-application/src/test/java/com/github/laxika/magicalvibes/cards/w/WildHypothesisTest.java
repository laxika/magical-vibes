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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
}
