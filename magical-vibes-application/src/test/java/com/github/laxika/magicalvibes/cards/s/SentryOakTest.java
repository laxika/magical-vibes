package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SentryOakTest extends BaseCardTest {

    private Permanent sentryOak() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Sentry Oak"))
                .findFirst().orElseThrow();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to BEGINNING_OF_COMBAT, trigger fires
        harness.passBothPriorities(); // resolve the MayEffect stack entry → may prompt
    }

    // ===== Won clash — +2/+0 and loses defender =====

    @Test
    @DisplayName("Winning the clash gives +2/+0 and removes defender until end of turn")
    void wonClashBoostsAndRemovesDefender() {
        harness.addToBattlefield(player1, new SentryOak());
        // Higher mana value on top for player1 (Grizzly Bears MV 2 > Forest MV 0) → player1 wins.
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        advanceToCombat(player1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent oak = sentryOak();
        assertThat(oak.getPowerModifier()).isEqualTo(2);
        assertThat(oak.getToughnessModifier()).isEqualTo(0);
        assertThat(oak.hasKeyword(Keyword.DEFENDER)).isFalse();
    }

    // ===== Lost clash — no boost, keeps defender =====

    @Test
    @DisplayName("Losing the clash leaves Sentry Oak unboosted and still with defender")
    void lostClashNoChange() {
        harness.addToBattlefield(player1, new SentryOak());
        // Lower mana value on top for player1 (Forest MV 0 < Grizzly Bears MV 2) → player1 loses.
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());
        gd.playerDecks.get(player2.getId()).addFirst(new GrizzlyBears());

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, true);

        Permanent oak = sentryOak();
        assertThat(oak.getPowerModifier()).isEqualTo(0);
        assertThat(oak.hasKeyword(Keyword.DEFENDER)).isTrue();
    }

    // ===== Declining the may ability =====

    @Test
    @DisplayName("Declining the clash leaves Sentry Oak unchanged")
    void declineClashNoChange() {
        harness.addToBattlefield(player1, new SentryOak());
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, false);

        Permanent oak = sentryOak();
        assertThat(oak.getPowerModifier()).isEqualTo(0);
        assertThat(oak.hasKeyword(Keyword.DEFENDER)).isTrue();
    }

    // ===== Does not trigger during opponent's combat =====

    @Test
    @DisplayName("Does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentCombat() {
        harness.addToBattlefield(player1, new SentryOak());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to BEGINNING_OF_COMBAT for player2

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    // ===== Boost and defender loss wear off at end of turn =====

    @Test
    @DisplayName("The boost and defender loss wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new SentryOak());
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, true);

        Permanent oak = sentryOak();
        assertThat(oak.getPowerModifier()).isEqualTo(2);
        assertThat(oak.hasKeyword(Keyword.DEFENDER)).isFalse();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(oak.getPowerModifier()).isEqualTo(0);
        assertThat(oak.hasKeyword(Keyword.DEFENDER)).isTrue();
    }
}
