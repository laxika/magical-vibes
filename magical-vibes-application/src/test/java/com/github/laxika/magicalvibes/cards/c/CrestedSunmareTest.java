package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CrestedSunmareTest extends BaseCardTest {

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to end step -> trigger queued
    }

    private long horseTokenCount(Player owner) {
        return gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Horse"))
                .count();
    }

    // ===== End-step token creation =====

    @Test
    @DisplayName("Creates a 5/5 white Horse token at end step if you gained life this turn")
    void createsHorseTokenWhenLifeGained() {
        harness.addToBattlefield(player1, new CrestedSunmare());
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        advanceToEndStep(player1);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        harness.passBothPriorities(); // resolve trigger -> token created

        var horses = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Horse"))
                .toList();
        assertThat(horses).hasSize(1);
        assertThat(horses).allSatisfy(t -> {
            assertThat(t.getCard().getPower()).isEqualTo(5);
            assertThat(t.getCard().getToughness()).isEqualTo(5);
            assertThat(t.getCard().isToken()).isTrue();
        });
    }

    @Test
    @DisplayName("Creates no token at end step if you did not gain life this turn")
    void noTokenWithoutLifeGain() {
        harness.addToBattlefield(player1, new CrestedSunmare());

        advanceToEndStep(player1);
        harness.passBothPriorities(); // resolve any trigger

        assertThat(horseTokenCount(player1)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Triggers on each end step, including the opponent's, when you gained life")
    void triggersOnOpponentEndStep() {
        harness.addToBattlefield(player1, new CrestedSunmare());
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        // It is the opponent's turn/end step.
        advanceToEndStep(player2);
        harness.passBothPriorities(); // resolve trigger -> token created

        assertThat(horseTokenCount(player1)).isEqualTo(1);
    }

    // ===== Static: other Horses have indestructible =====

    @Test
    @DisplayName("Two Crested Sunmares grant each other indestructible")
    void twoSunmaresGrantEachOtherIndestructible() {
        harness.addToBattlefield(player1, new CrestedSunmare());
        harness.addToBattlefield(player1, new CrestedSunmare());

        List<Permanent> sunmares = gd.playerBattlefields.get(player1.getId());
        assertThat(sunmares).hasSize(2);
        assertThat(sunmares).allSatisfy(s ->
                assertThat(gqs.hasKeyword(gd, s, Keyword.INDESTRUCTIBLE)).isTrue());
    }

    @Test
    @DisplayName("A lone Crested Sunmare does not grant itself indestructible")
    void loneSunmareNotIndestructible() {
        harness.addToBattlefield(player1, new CrestedSunmare());

        Permanent sunmare = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.hasKeyword(gd, sunmare, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Two Crested Sunmares both survive Wrath of God; a lone one does not")
    void indestructibleSunmaresSurviveWrath() {
        harness.addToBattlefield(player1, new CrestedSunmare());
        harness.addToBattlefield(player1, new CrestedSunmare());

        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        // Each grants the other indestructible -> both survive.
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Crested Sunmare"))
                .count()).isEqualTo(2);
    }

    @Test
    @DisplayName("A lone Crested Sunmare is destroyed by Wrath of God")
    void loneSunmareDiesToWrath() {
        harness.addToBattlefield(player1, new CrestedSunmare());

        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Crested Sunmare"));
    }
}
