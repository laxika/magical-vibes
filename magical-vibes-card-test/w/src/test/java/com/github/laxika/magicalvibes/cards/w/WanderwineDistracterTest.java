package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WanderwineDistracter.class, GrizzlyBears.class})
class WanderwineDistracterTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming tapped queues a target choice and gives an opponent creature -3/-0")
    void becomingTappedDebuffsOpponentCreature() {
        Permanent distracter = harness.addToBattlefieldAndReturn(player1, new WanderwineDistracter());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        tap(distracter);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(opponentCreature.getId())
                .doesNotContain(ownCreature.getId());

        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.getPowerModifier()).isEqualTo(-3);
        assertThat(opponentCreature.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Tapping another permanent you control does not trigger")
    void tappingAnotherPermanentDoesNotTrigger() {
        harness.addToBattlefield(player1, new WanderwineDistracter());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        tap(otherCreature);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The temporary debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent distracter = harness.addToBattlefieldAndReturn(player1, new WanderwineDistracter());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        tap(distracter);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(opponentCreature.getPowerModifier()).isZero();
        assertThat(opponentCreature.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Without an opponent creature the trigger does not require a target choice")
    void noLegalTargetsDoesNotLeavePendingChoice() {
        Permanent distracter = harness.addToBattlefieldAndReturn(player1, new WanderwineDistracter());
        harness.addToBattlefield(player1, new WanderwineDistracter());

        tap(distracter);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(distracter.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Two separate tap events in one turn each apply the debuff")
    void repeatedTapEventsStackDebuffs() {
        Permanent distracter = harness.addToBattlefieldAndReturn(player1, new WanderwineDistracter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WanderwineDistracter());

        tap(distracter);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        distracter.untap();
        tap(distracter);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-6);
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A second Distracter does not trigger when the first becomes tapped")
    void anotherCopyDoesNotTrigger() {
        Permanent distracter = harness.addToBattlefieldAndReturn(player1, new WanderwineDistracter());
        harness.addToBattlefield(player1, new WanderwineDistracter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WanderwineDistracter());

        tap(distracter);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-3);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The trigger resolves after its source leaves the battlefield")
    void triggerResolvesWithoutItsSource() {
        Permanent distracter = harness.addToBattlefieldAndReturn(player1, new WanderwineDistracter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WanderwineDistracter());

        tap(distracter);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(distracter);
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-3);
        assertThat(target.getToughnessModifier()).isZero();
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
