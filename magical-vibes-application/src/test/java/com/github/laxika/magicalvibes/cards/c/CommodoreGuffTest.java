package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HuatliRadiantChampion;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CommodoreGuff.class, HuatliRadiantChampion.class, Shock.class})
class CommodoreGuffTest extends BaseCardTest {

    @Test
    @DisplayName("End-step trigger puts a loyalty counter on another planeswalker you control")
    void endStepAddsLoyaltyToAnotherControlledPlaneswalker() {
        addReadyGuff(5);
        Permanent huatli = addReadyHuatli(3);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
            harness.handlePermanentChosen(player1, huatli.getId());
        }
        harness.passBothPriorities();

        assertThat(huatli.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("End-step trigger cannot target an opponent's planeswalker")
    void endStepTriggerRequiresControlledPlaneswalker() {
        addReadyGuff(5);
        addReadyHuatli(player2, 3);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanent(player2, "Huatli, Radiant Champion")
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
        addReadyHuatli(3);
        harness.setLife(player2, 20);
        Card first = new Shock();
        Card second = new Shock();
        harness.setLibrary(player1, new ArrayList<>(List.of(first, second)));
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

    private Permanent addReadyGuff(int loyalty) {
        return addReadyGuff(player1, loyalty);
    }

    private Permanent addReadyGuff(Player player, int loyalty) {
        Permanent perm = new Permanent(new CommodoreGuff());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addReadyHuatli(int loyalty) {
        return addReadyHuatli(player1, loyalty);
    }

    private Permanent addReadyHuatli(Player player, int loyalty) {
        Permanent perm = new Permanent(new HuatliRadiantChampion());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
