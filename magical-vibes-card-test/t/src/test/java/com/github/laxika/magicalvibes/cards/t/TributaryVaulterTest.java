package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MerrowCommerce;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TributaryVaulter.class, CoralMerfolk.class, GrizzlyBears.class, MerrowCommerce.class})
class TributaryVaulterTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Tributary Vaulter lets another Merfolk get +2/+0")
    void tappingControlledMerfolkBoostsAnotherMerfolk() {
        Permanent vaulter = addCreatureReady(player1, new TributaryVaulter());
        Permanent targetMerfolk = addCreatureReady(player1, new CoralMerfolk());

        tap(vaulter);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetMerfolk.getId());
        harness.passBothPriorities();

        assertThat(targetMerfolk.getPowerModifier()).isEqualTo(2);
        assertThat(targetMerfolk.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The trigger can target only another Merfolk you control")
    void triggerRestrictsTargets() {
        Permanent vaulter = addCreatureReady(player1, new TributaryVaulter());
        Permanent otherMerfolk = addCreatureReady(player1, new CoralMerfolk());
        Permanent targetMerfolk = addCreatureReady(player1, new CoralMerfolk());
        Permanent nonMerfolk = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentMerfolk = addCreatureReady(player2, new CoralMerfolk());

        tap(vaulter);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(targetMerfolk.getId())
                .contains(otherMerfolk.getId())
                .doesNotContain(vaulter.getId(), nonMerfolk.getId(), opponentMerfolk.getId());

        harness.handlePermanentChosen(player1, targetMerfolk.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent vaulter = addCreatureReady(player1, new TributaryVaulter());
        Permanent targetMerfolk = addCreatureReady(player1, new CoralMerfolk());

        tap(vaulter);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetMerfolk.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(targetMerfolk.getPowerModifier()).isZero();
    }

    @Test
    void tappingAnotherMerfolkDoesNotTrigger() {
        addCreatureReady(player1, new TributaryVaulter());
        Permanent otherMerfolk = addCreatureReady(player1, new CoralMerfolk());

        tap(otherMerfolk);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(otherMerfolk.getPowerModifier()).isZero();
    }

    @Test
    void tappingOpponentsVaulterDoesNotTriggerYourVaulter() {
        addCreatureReady(player1, new TributaryVaulter());
        Permanent ownMerfolk = addCreatureReady(player1, new CoralMerfolk());
        Permanent opponentVaulter = addCreatureReady(player2, new TributaryVaulter());
        Permanent opponentMerfolk = addCreatureReady(player2, new CoralMerfolk());

        tap(opponentVaulter);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, opponentMerfolk.getId());
        harness.passBothPriorities();

        assertThat(ownMerfolk.getPowerModifier()).isZero();
        assertThat(opponentMerfolk.getPowerModifier()).isEqualTo(2);
    }

    @Test
    void repeatedTapEventsStackTheirBoosts() {
        Permanent vaulter = addCreatureReady(player1, new TributaryVaulter());
        Permanent targetMerfolk = addCreatureReady(player1, new CoralMerfolk());

        tap(vaulter);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetMerfolk.getId());
        harness.passBothPriorities();
        vaulter.untap();
        tap(vaulter);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetMerfolk.getId());
        harness.passBothPriorities();

        assertThat(targetMerfolk.getPowerModifier()).isEqualTo(4);
        assertThat(targetMerfolk.getToughnessModifier()).isZero();
    }

    @Test
    void triggerResolvesAfterVaulterLeavesBattlefield() {
        Permanent vaulter = addCreatureReady(player1, new TributaryVaulter());
        Permanent targetMerfolk = addCreatureReady(player1, new CoralMerfolk());

        tap(vaulter);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetMerfolk.getId());
        gd.playerBattlefields.get(player1.getId()).remove(vaulter);
        harness.passBothPriorities();

        assertThat(targetMerfolk.getPowerModifier()).isEqualTo(2);
    }

    @Test
    void noncreatureMerfolkIsALegalTarget() {
        Permanent vaulter = addCreatureReady(player1, new TributaryVaulter());
        Permanent targetMerfolk = addCreatureReady(player1, new CoralMerfolk());
        Permanent commerce = new Permanent(new MerrowCommerce());
        gd.playerBattlefields.get(player1.getId()).add(commerce);

        tap(vaulter);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(commerce.getId());

        harness.handlePermanentChosen(player1, targetMerfolk.getId());
        harness.passBothPriorities();
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(() -> {
            harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent);
            harness.getTriggerCollectionService().processNextEntersTriggerTarget(gd);
        });
    }
}
