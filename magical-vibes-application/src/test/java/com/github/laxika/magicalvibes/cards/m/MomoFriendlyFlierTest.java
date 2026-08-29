package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NimbleObstructionist;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MomoFriendlyFlier.class, GrizzlyBears.class, NimbleObstructionist.class, SerraAngel.class})
class MomoFriendlyFlierTest extends BaseCardTest {

    @Test
    @DisplayName("The first non-Lemur flying creature spell each turn costs {1} less")
    void firstNonLemurFlyingCreatureSpellIsReduced() {
        addMomo();
        harness.setHand(player1, List.of(new SerraAngel()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Only the first matching flying creature spell each turn is reduced")
    void onlyFirstMatchingSpellIsReduced() {
        addMomo();
        harness.setHand(player1, List.of(new SerraAngel(), new SerraAngel()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A nonflying creature spell does not consume the reduction")
    void nonflyingCreatureDoesNotConsumeReduction() {
        addMomo();
        harness.setHand(player1, List.of(new GrizzlyBears(), new SerraAngel()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Serra Angel"));
    }

    @Test
    @DisplayName("The cost reduction applies only during Momo's controller's turn")
    void reductionDoesNotApplyDuringOpponentTurn() {
        addMomo();
        harness.setHand(player1, List.of(new NimbleObstructionist()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Momo gets +1/+1 when another flying creature enters")
    void flyingCreatureEnteringBoostsMomo() {
        Permanent momo = addMomo();
        harness.setHand(player1, List.of(new SerraAngel()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, momo)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, momo)).isEqualTo(2);
    }

    @Test
    @DisplayName("A nonflying creature entering does not boost Momo")
    void nonflyingCreatureEnteringDoesNotBoostMomo() {
        Permanent momo = addMomo();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, momo)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, momo)).isEqualTo(1);
    }

    private Permanent addMomo() {
        return harness.addToBattlefieldAndReturn(player1, new MomoFriendlyFlier());
    }
}
