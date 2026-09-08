package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TotentanzSwarmPiper.class, GrizzlyBears.class})
class TotentanzSwarmPiperTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a nonblocking Rat when another nontoken creature you control dies")
    void createsRatWhenAnotherNontokenCreatureDies() {
        addCreatureReady(player1, new TotentanzSwarmPiper());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        destroyToGraveyard(bears);

        Permanent rat = findPermanent(player1, "Rat");
        assertThat(bls.canBlock(gd, rat)).isFalse();
    }

    @Test
    @DisplayName("Creates a Rat when Totentanz dies")
    void createsRatWhenTotentanzDies() {
        Permanent totentanz = addCreatureReady(player1, new TotentanzSwarmPiper());

        destroyToGraveyard(totentanz);

        assertThat(findPermanents(player1, "Rat")).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger when a Rat token dies")
    void doesNotTriggerForRatToken() {
        addCreatureReady(player1, new TotentanzSwarmPiper());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        destroyToGraveyard(bears);

        Permanent rat = findPermanent(player1, "Rat");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, rat));

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Rat")).isEmpty();
    }

    @Test
    @DisplayName("Gives an attacking Rat deathtouch until end of turn")
    void givesAttackingRatDeathtouchUntilEndOfTurn() {
        addCreatureReady(player1, new TotentanzSwarmPiper());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        destroyToGraveyard(bears);

        Permanent rat = findPermanent(player1, "Rat");
        rat.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, rat.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, rat, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, rat, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Rejects a non-Rat attacking target")
    void rejectsNonRatTarget() {
        addCreatureReady(player1, new TotentanzSwarmPiper());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void destroyToGraveyard(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
    }
}
