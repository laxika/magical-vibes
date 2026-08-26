package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VenerableKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.setHand(player1, List.of(new VenerableKnight()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
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
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
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
        harness.setHand(player2, List.of(new VenerableKnight()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(trebuchet.isTapped()).isTrue();
    }
}
