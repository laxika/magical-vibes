package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ExpeditionHealer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeaGateBanneret.class, ExpeditionHealer.class})
class SeaGateBanneretTest extends BaseCardTest {

    @Test
    void activatedAbilityBoostsCreaturesYouControl() {
        Permanent banneret = addCreatureReady(player1, new SeaGateBanneret());
        Permanent ownCreature = addCreatureReady(player1, new ExpeditionHealer());
        Permanent opponentCreature = addCreatureReady(player2, new ExpeditionHealer());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, banneret)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, banneret)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    void activatedAbilityBoostWearsOffAtEndOfTurn() {
        Permanent ownCreature = addCreatureReady(player1, new ExpeditionHealer());
        addCreatureReady(player1, new SeaGateBanneret());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
    }

    @Test
    void repeatedActivationsStackEvenWhileTappedAndSummoningSick() {
        Permanent banneret = harness.addToBattlefieldAndReturn(player1, new SeaGateBanneret());
        banneret.setSummoningSick(true);
        banneret.tap();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, banneret)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, banneret)).isEqualTo(4);
        assertThat(banneret.isTapped()).isTrue();
    }

    @Test
    void onlyCreaturesPresentAtResolutionReceiveTheBoost() {
        Permanent banneret = addCreatureReady(player1, new SeaGateBanneret());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        Permanent beforeResolution = harness.enterBattlefieldAndReturn(player1, new ExpeditionHealer());
        harness.passBothPriorities();
        Permanent afterResolution = harness.enterBattlefieldAndReturn(player1, new ExpeditionHealer());

        assertThat(gqs.getEffectivePower(gd, banneret)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, banneret)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, beforeResolution)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, afterResolution)).isEqualTo(2);
    }

    @Test
    void abilityStillResolvesAfterBanneretLeavesTheBattlefield() {
        Permanent banneret = addCreatureReady(player1, new SeaGateBanneret());
        Permanent ownCreature = addCreatureReady(player1, new ExpeditionHealer());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(banneret);
        gd.playerGraveyards.get(player1.getId()).add(banneret.getCard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
    }
}
