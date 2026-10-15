package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.Levitation;
import com.github.laxika.magicalvibes.cards.n.NimbleObstructionist;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MomoFriendlyFlier.class, GrizzlyBears.class, NimbleObstructionist.class, SerraAngel.class,
        Levitation.class})
class MomoFriendlyFlierTest extends BaseCardTest {

    @Test
    @DisplayName("The first non-Lemur flying creature spell each turn costs {1} less")
    void firstNonLemurFlyingCreatureSpellIsReduced() {
        addMomo();
        harness.setHand(player1, List.of(new SerraAngel()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Only the first matching flying creature spell each turn is reduced")
    void onlyFirstMatchingSpellIsReduced() {
        addMomo();
        harness.setHand(player1, List.of(new SerraAngel(), new SerraAngel()));
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A nonflying creature spell does not consume the reduction")
    void nonflyingCreatureDoesNotConsumeReduction() {
        addMomo();
        harness.setHand(player1, List.of(new GrizzlyBears(), new SerraAngel()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
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
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

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
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, momo)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, momo)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting Momo does not consume the non-Lemur spell reduction")
    void lemurSpellDoesNotConsumeReduction() {
        harness.setHand(player1, List.of(new MomoFriendlyFlier(), new SerraAngel()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(SerraAngel.class);
    }

    @Test
    @DisplayName("A matching spell cast before Momo entered still consumes the first spell")
    void earlierMatchingSpellCountsBeforeMomoEnters() {
        harness.setHand(player1, List.of(new SerraAngel(), new SerraAngel()));
        harness.addMana(player1, ManaColor.WHITE, 9);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        addMomo();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction does not pay the colored portion of a flying creature's cost")
    void coloredManaRequirementsAreNotReduced() {
        addMomo();
        harness.setHand(player1, List.of(new SerraAngel()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Momo does not reduce an opponent's flying creature spell")
    void opponentSpellIsNotReduced() {
        addMomo();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new SerraAngel()));
        harness.addMana(player2, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction becomes available again on the controller's next turn")
    void reductionResetsOnNextOwnTurn() {
        addMomo();
        harness.setHand(player1, List.of(new SerraAngel()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SerraAngel()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(SerraAngel.class);
    }

    @Test
    @DisplayName("Momo does not trigger for its own entry")
    void ownEntryDoesNotBoostMomo() {
        Permanent momo = harness.enterBattlefieldAndReturn(player1, new MomoFriendlyFlier());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, momo)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, momo)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's flying creature entering does not boost Momo")
    void opponentFlyingCreatureDoesNotBoostMomo() {
        Permanent momo = addMomo();
        harness.enterBattlefieldAndReturn(player2, new SerraAngel());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, momo)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, momo)).isEqualTo(1);
    }

    @Test
    @DisplayName("Flying creatures entering without being cast give cumulative boosts until cleanup")
    void boostsAccumulateAndExpireAtEndOfTurn() {
        Permanent momo = addMomo();
        harness.enterBattlefieldAndReturn(player1, new SerraAngel());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player1, new SerraAngel());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, momo)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, momo)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, momo)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, momo)).isEqualTo(1);
    }

    @Test
    @DisplayName("Flying granted by a continuous effect counts for Momo's entry trigger")
    void grantedFlyingOnEntryBoostsMomo() {
        Permanent momo = addMomo();
        harness.addToBattlefield(player1, new Levitation());

        Permanent bear = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isTrue();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, momo)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, momo)).isEqualTo(2);
    }

    @Test
    @DisplayName("Flying granted only on the battlefield does not qualify a spell for the discount")
    void battlefieldFlyingGrantDoesNotReduceNonflyingSpell() {
        addMomo();
        harness.addToBattlefield(player1, new Levitation());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addMomo() {
        return harness.addToBattlefieldAndReturn(player1, new MomoFriendlyFlier());
    }
}
