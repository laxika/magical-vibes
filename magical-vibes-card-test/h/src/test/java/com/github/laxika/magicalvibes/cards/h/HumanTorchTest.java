package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BurstOfStrength;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HumanTorch.class, BurstOfStrength.class, GrizzlyBears.class})
class HumanTorchTest extends BaseCardTest {

    @Test
    @DisplayName("Gains flying, double strike, and haste after a noncreature spell")
    void gainsKeywordsAfterNoncreatureSpell() {
        Permanent torch = addCreatureReady(player1, new HumanTorch());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        advanceToBeginningOfCombat();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, torch, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, torch, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, torch, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not gain the beginning-of-combat keywords after only a creature spell")
    void doesNotGainKeywordsAfterCreatureSpell() {
        Permanent torch = addCreatureReady(player1, new HumanTorch());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        advanceToBeginningOfCombat();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, torch, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, torch, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, torch, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Paying on attack grants the combat-damage trigger without damaging the attacked opponent twice")
    void payingOnAttackDamagesOnlyOtherOpponents() {
        addCreatureReady(player1, new HumanTorch());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Casting a noncreature spell after combat begins does not create the combat-start trigger")
    void noncreatureSpellAfterCombatBeginsIsTooLate() {
        Permanent torch = addCreatureReady(player1, new HumanTorch());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        advanceToBeginningOfCombat();
        assertThat(gd.stack).isEmpty();
        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, torch, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, torch, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, torch, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The combat-start keywords expire at the end of the turn")
    void combatKeywordsExpireAtEndOfTurn() {
        Permanent torch = addCreatureReady(player1, new HumanTorch());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        advanceToBeginningOfCombat();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, torch, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, torch, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, torch, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, torch, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Declining the attack payment preserves mana and still deals ordinary combat damage")
    void decliningAttackPaymentPreservesMana() {
        addCreatureReady(player1, new HumanTorch());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMayAbilityChosen(player1, false));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);

        resolveCombat();
        resolveAllTriggers();
        harness.assertLife(player2, 17);
    }

    private void advanceToBeginningOfCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
    }
}
