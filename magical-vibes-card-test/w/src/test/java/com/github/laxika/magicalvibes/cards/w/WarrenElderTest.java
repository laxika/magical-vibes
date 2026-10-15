package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarrenElder.class})
class WarrenElderTest extends BaseCardTest {

    @Test
    void boostsCreaturesYouControlUntilEndOfTurn() {
        Permanent elder = addCreatureReady(player1, new WarrenElder());
        Permanent ownCreature = addCreatureReady(player1, new WarrenElder());
        Permanent opponentCreature = addCreatureReady(player2, new WarrenElder());
        addManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elder)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elder)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    void boostWearsOffAtCleanup() {
        addCreatureReady(player1, new WarrenElder());
        Permanent ownCreature = addCreatureReady(player1, new WarrenElder());
        addManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent elder = addCreatureReady(player1, new WarrenElder());
        elder.setSummoningSick(true);
        elder.tap();
        addManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elder)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elder)).isEqualTo(3);
        assertThat(elder.isTapped()).isTrue();
    }

    @Test
    void repeatedActivationsStackTheirBoosts() {
        Permanent elder = addCreatureReady(player1, new WarrenElder());
        addManaForAbility();
        harness.activateAbility(player1, 0, null, null);
        addManaForAbility();
        harness.activateAbility(player1, 0, null, null);

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, elder)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elder)).isEqualTo(4);
        assertThat(elder.isTapped()).isFalse();
    }

    @Test
    void affectsCreaturesPresentAtResolutionButNotLaterArrivals() {
        addCreatureReady(player1, new WarrenElder());
        addManaForAbility();
        harness.activateAbility(player1, 0, null, null);
        Permanent beforeResolution = addCreatureReady(player1, new WarrenElder());

        harness.passBothPriorities();
        Permanent afterResolution = addCreatureReady(player1, new WarrenElder());

        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, beforeResolution)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, afterResolution)).isEqualTo(2);
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
