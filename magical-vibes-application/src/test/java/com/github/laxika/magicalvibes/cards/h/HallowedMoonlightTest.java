package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GatherTheTownsfolk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HallowedMoonlightTest extends BaseCardTest {

    private void castHallowedMoonlight(Player caster) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(caster, List.of(new HallowedMoonlight()));
        harness.addMana(caster, ManaColor.WHITE, 2);
        harness.castInstant(caster, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    private String nameOf(Permanent p) {
        return p.getCard().getName();
    }

    private void castGatherTheTownsfolk(Player caster) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(caster, List.of(new GatherTheTownsfolk()));
        harness.addMana(caster, ManaColor.WHITE, 2);
        harness.castSorcery(caster, 0, 0);
        harness.passBothPriorities();
    }

    private long humanTokenCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken() && "Human".equals(nameOf(p)))
                .count();
    }

    @Test
    @DisplayName("Creature tokens that would enter this turn never appear")
    void tokensDoNotEnter() {
        castHallowedMoonlight(player1);

        castGatherTheTownsfolk(player2);

        assertThat(humanTokenCount(player2)).isZero();
        assertThat(humanTokenCount(player1)).isZero();
    }

    @Test
    @DisplayName("A creature that was cast still enters normally")
    void castCreatureStillEnters() {
        castHallowedMoonlight(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(this::nameOf).contains("Grizzly Bears");
    }

    @Test
    @DisplayName("It is symmetrical — the caster's own tokens are exiled too")
    void ownTokensAlsoReplaced() {
        castHallowedMoonlight(player1);

        castGatherTheTownsfolk(player1);

        assertThat(humanTokenCount(player1)).isZero();
    }

    @Test
    @DisplayName("Resolving Hallowed Moonlight draws a card")
    void drawsACard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new HallowedMoonlight()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The replacement wears off at end of turn")
    void wearsOffNextTurn() {
        castHallowedMoonlight(player1);
        advanceToNextTurn(player1);

        castGatherTheTownsfolk(player2);

        assertThat(humanTokenCount(player2)).isEqualTo(2);
    }

    @Test
    @DisplayName("Without Hallowed Moonlight, tokens enter as normal")
    void baselineTokensEnter() {
        castGatherTheTownsfolk(player2);

        assertThat(humanTokenCount(player2)).isEqualTo(2);
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn
    }
}
