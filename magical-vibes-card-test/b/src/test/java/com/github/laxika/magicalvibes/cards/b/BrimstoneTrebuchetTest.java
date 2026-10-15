package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VenerableKnight;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrimstoneTrebuchet.class, VenerableKnight.class, GrizzlyBears.class})
class BrimstoneTrebuchetTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability deals 1 damage to each opponent")
    void tapAbilityDamagesEachOpponent() {
        Permanent trebuchet = addCreatureReady(player1, new BrimstoneTrebuchet());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(trebuchet.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A Knight entering under your control untaps Brimstone Trebuchet")
    void knightEnteringUntapsTrebuchet() {
        Permanent trebuchet = addCreatureReady(player1, new BrimstoneTrebuchet());
        trebuchet.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new VenerableKnight(), "{W}");
        resolveAllTriggers();

        assertThat(trebuchet.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A non-Knight creature entering does not untap Brimstone Trebuchet")
    void nonKnightEnteringDoesNotUntapTrebuchet() {
        Permanent trebuchet = addCreatureReady(player1, new BrimstoneTrebuchet());
        trebuchet.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        assertThat(trebuchet.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's Knight entering does not untap Brimstone Trebuchet")
    void opponentKnightEnteringDoesNotUntapTrebuchet() {
        Permanent trebuchet = addCreatureReady(player1, new BrimstoneTrebuchet());
        trebuchet.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new VenerableKnight(), "{W}");
        resolveAllTriggers();

        assertThat(trebuchet.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The tap ability cannot be activated while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent trebuchet = harness.addToBattlefieldAndReturn(player1, new BrimstoneTrebuchet());
        trebuchet.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(trebuchet.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Trebuchet cannot activate its tap ability")
    void cannotActivateWhileTapped() {
        Permanent trebuchet = addCreatureReady(player1, new BrimstoneTrebuchet());
        trebuchet.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(trebuchet.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Knight triggers while Trebuchet is untapped and can untap it after a response")
    void canActivateInResponseToUntapTriggerAndAgainAfterResolution() {
        Permanent trebuchet = addCreatureReady(player1, new BrimstoneTrebuchet());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new VenerableKnight(), "{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(trebuchet.isTapped()).isFalse();

        harness.activateAbility(player1, 0, null, null);
        assertThat(trebuchet.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        assertThat(trebuchet.isTapped()).isTrue();

        resolveAllTriggers();
        assertThat(trebuchet.isTapped()).isFalse();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        harness.assertLife(player2, 18);
        assertThat(trebuchet.isTapped()).isTrue();
    }
}
