package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AngelOfRetribution;
import com.github.laxika.magicalvibes.cards.c.CabalCoffers;
import com.github.laxika.magicalvibes.cards.c.CabalRitual;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Narcissism.class, AngelOfRetribution.class, CabalCoffers.class, CabalRitual.class})
class NarcissismTest extends BaseCardTest {

    @Test
    void discardingACardBoostsTargetCreature() {
        addNarcissism();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelOfRetribution());
        harness.setHand(player1, List.of(new CabalRitual()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        int basePower = gqs.getEffectivePower(gd, target);
        int baseToughness = gqs.getEffectiveToughness(gd, target);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(baseToughness + 2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        harness.assertInGraveyard(player1, "Cabal Ritual");
    }

    @Test
    void sacrificingNarcissismBoostsTargetCreature() {
        addNarcissism();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelOfRetribution());
        harness.addMana(player1, ManaColor.GREEN, 1);
        int basePower = gqs.getEffectivePower(gd, target);
        int baseToughness = gqs.getEffectiveToughness(gd, target);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(baseToughness + 2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        harness.assertInGraveyard(player1, "Narcissism");
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        addNarcissism();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AngelOfRetribution());
        harness.setHand(player1, List.of(new CabalRitual()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        int basePower = gqs.getEffectivePower(gd, target);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(basePower + 2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(basePower);
    }

    @Test
    void cannotActivateDiscardAbilityWithoutACardInHand() {
        addNarcissism();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelOfRetribution());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        addNarcissism();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CabalCoffers());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new CabalRitual()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        harness.assertInHand(player1, "Cabal Ritual");
    }

    @Test
    void sacrificingAbilityCannotTargetNonCreaturePermanent() {
        addNarcissism();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CabalCoffers());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Narcissism");
    }

    private void addNarcissism() {
        harness.addToBattlefield(player1, new Narcissism());
    }
}
