package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.t.TeferiTemporalArchmage;
import com.github.laxika.magicalvibes.cards.j.JayasPhoenix;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CommodoreGuff.class, TeferiTemporalArchmage.class, JayasPhoenix.class, ChandraLegacyOfFire.class})
class CommodoreGuffTest extends BaseCardTest {

    @Test
    @DisplayName("End-step trigger puts a loyalty counter on another planeswalker you control")
    void endStepAddsLoyaltyToAnotherControlledPlaneswalker() {
        addReadyGuff(5);
        Permanent teferi = addReadyTeferi(3);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
            harness.handlePermanentChosen(player1, teferi.getId());
        }
        harness.passBothPriorities();

        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("End-step trigger cannot target an opponent's planeswalker")
    void endStepTriggerRequiresControlledPlaneswalker() {
        addReadyGuff(5);
        addReadyTeferi(player2, 3);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanent(player2, "Teferi, Temporal Archmage")
                .getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("+1 creates a red Wizard token with planeswalker-only red mana")
    void plusOneCreatesRestrictedWizardToken() {
        Permanent guff = addReadyGuff(5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Wizard");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.WIZARD);
        assertThat(guff.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);

        token.setSummoningSick(false);
        int tokenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(token);
        harness.activateAbility(player1, tokenIndex, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId())
                .getSubtypeOrPlaneswalkerSpellManaTotal(Set.of(
                        new ManaRestriction.SubtypeOrPlaneswalkerSpells()
                ))).isEqualTo(1);
    }

    @Test
    @DisplayName("−3 draws and damages for the number of planeswalkers you control")
    void minusThreeUsesControlledPlaneswalkerCount() {
        Permanent guff = addReadyGuff(5);
        addReadyTeferi(3);
        harness.setLife(player2, 20);
        Card first = new JayasPhoenix();
        Card second = new JayasPhoenix();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(guff.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("−3 cannot be activated with insufficient loyalty")
    void cannotActivateMinusThreeWithInsufficientLoyalty() {
        addReadyGuff(2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    @Test
    void endStepCannotTargetGuffItself() {
        Permanent guff = addReadyGuff(5);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(guff.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void minusThreeDoesNotCountGuffAfterPayingItsLastLoyalty() {
        addReadyGuff(3);
        addReadyTeferi(3);
        harness.setHand(player1, List.of());
        Card drawn = new JayasPhoenix();
        harness.setLibrary(player1, List.of(drawn, new JayasPhoenix()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Commodore Guff");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    void minusThreeWithNoRemainingPlaneswalkersDrawsAndDealsNothing() {
        addReadyGuff(3);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new JayasPhoenix()));
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Commodore Guff");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void minusThreeExcludesOpponentsPlaneswalkers() {
        addReadyGuff(5);
        addReadyTeferi(player2, 3);
        harness.setHand(player1, List.of());
        Card drawn = new JayasPhoenix();
        harness.setLibrary(player1, List.of(drawn, new JayasPhoenix()));
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertLife(player2, 19);
    }

    @Test
    void minusThreeCountsPlaneswalkersAtResolution() {
        addReadyGuff(5);
        Permanent teferi = addReadyTeferi(3);
        harness.setHand(player1, List.of());
        Card drawn = new JayasPhoenix();
        harness.setLibrary(player1, List.of(drawn, new JayasPhoenix()));
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        teferi.setCounterCount(CounterType.LOYALTY, 0);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertLife(player2, 19);
    }

    @Test
    void wizardCannotTapForManaWhileSummoningSick() {
        addReadyGuff(5);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void wizardManaPaysForPlaneswalkerButNotCreatureSpell() {
        addReadyGuff(5);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        Permanent wizard = gd.playerBattlefields.get(player1.getId()).get(1);
        wizard.setSummoningSick(false);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setHand(player1, List.of(new JayasPhoenix()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new ChandraLegacyOfFire()));
        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Chandra, Legacy of Fire");
        assertThat(wizard.isTapped()).isTrue();
    }

    private Permanent addReadyGuff(int loyalty) {
        return addReadyGuff(player1, loyalty);
    }

    private Permanent addReadyGuff(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new CommodoreGuff());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addReadyTeferi(int loyalty) {
        return addReadyTeferi(player1, loyalty);
    }

    private Permanent addReadyTeferi(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new TeferiTemporalArchmage());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        return perm;
    }
}
