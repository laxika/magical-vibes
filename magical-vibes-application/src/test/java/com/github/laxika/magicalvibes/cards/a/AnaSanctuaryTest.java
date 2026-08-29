package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MerfolkOfThePearlTrident;
import com.github.laxika.magicalvibes.cards.n.NightveilSpecter;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnaSanctuary.class, GrizzlyBears.class, MerfolkOfThePearlTrident.class,
        NightveilSpecter.class, ScatheZombies.class})
class AnaSanctuaryTest extends BaseCardTest {

    @Test
    @DisplayName("A blue permanent gives the target creature +1/+1")
    void boostsByOneWithBluePermanentOnly() {
        Permanent target = setUpWithTarget(new MerfolkOfThePearlTrident());

        resolveTarget(target);

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("A black permanent gives the target creature +1/+1")
    void boostsByOneWithBlackPermanentOnly() {
        Permanent target = setUpWithTarget(new ScatheZombies());

        resolveTarget(target);

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Blue and black permanents give the target creature +5/+5")
    void boostsByFiveWithBlueAndBlackPermanents() {
        harness.addToBattlefield(player1, new AnaSanctuary());
        harness.addToBattlefield(player1, new MerfolkOfThePearlTrident());
        harness.addToBattlefield(player1, new ScatheZombies());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        resolveTarget(target);

        assertThat(target.getPowerModifier()).isEqualTo(5);
        assertThat(target.getToughnessModifier()).isEqualTo(5);
    }

    @Test
    @DisplayName("One blue-black permanent gives the target creature +5/+5")
    void boostsByFiveWithOneBlueBlackPermanent() {
        harness.addToBattlefield(player1, new AnaSanctuary());
        harness.addToBattlefield(player1, new NightveilSpecter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        resolveTarget(target);

        assertThat(target.getPowerModifier()).isEqualTo(5);
        assertThat(target.getToughnessModifier()).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not trigger without a blue or black permanent")
    void doesNotTriggerWithoutBlueOrBlackPermanent() {
        harness.addToBattlefield(player1, new AnaSanctuary());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = setUpWithTarget(new MerfolkOfThePearlTrident());

        resolveTarget(target);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    private Permanent setUpWithTarget(com.github.laxika.magicalvibes.model.Card supportPermanent) {
        harness.addToBattlefield(player1, new AnaSanctuary());
        harness.addToBattlefield(player1, supportPermanent);
        return harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
    }

    private void resolveTarget(Permanent target) {
        advanceToUpkeep(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
