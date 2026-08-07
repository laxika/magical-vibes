package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AbeyanceTest extends BaseCardTest {

    /** Player1 casts Abeyance at player2 on player1's postcombat main phase. */
    private void castAbeyanceAtPlayer2() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Abeyance()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Target player can't cast an instant")
    void targetCantCastInstant() {
        castAbeyanceAtPlayer2();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Target player can still cast a creature spell")
    void targetCanStillCastCreature() {
        castAbeyanceAtPlayer2();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Target player can't activate a non-mana ability")
    void targetCantActivateNonManaAbility() {
        castAbeyanceAtPlayer2();

        Permanent vanguard = harness.addToBattlefieldAndReturn(player2, new AdantoVanguard());
        vanguard.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("aren't mana abilities");
    }

    @Test
    @DisplayName("Target player can still activate mana abilities")
    void targetCanStillActivateManaAbility() {
        castAbeyanceAtPlayer2();

        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        elves.setSummoningSick(false);

        harness.tapPermanent(player2, 0);

        assertThat(elves.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Controller draws a card")
    void controllerDrawsACard() {
        castAbeyanceAtPlayer2();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Restrictions wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        castAbeyanceAtPlayer2();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent vanguard = harness.addToBattlefieldAndReturn(player2, new AdantoVanguard());
        vanguard.setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }
}
