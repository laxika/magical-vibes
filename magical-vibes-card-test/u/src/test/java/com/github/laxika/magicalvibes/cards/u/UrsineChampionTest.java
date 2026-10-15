package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UrsineChampion.class})
class UrsineChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Ability gives +3/+3 and makes Ursine Champion a Bear Berserker")
    void boostsAndChangesTypes() {
        Permanent champion = addReadyChampion(player1);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(champion.getEffectivePower()).isEqualTo(5);
        assertThat(champion.getEffectiveToughness()).isEqualTo(5);
        assertThat(gqs.effectiveCreatureSubtypes(gd, champion))
                .containsExactlyInAnyOrder(CardSubtype.BEAR, CardSubtype.BERSERKER);
    }

    @Test
    @DisplayName("Ability is limited to once each turn and wears off at cleanup")
    void limitedAndTemporary() {
        Permanent champion = addReadyChampion(player1);
        addAbilityMana(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(champion.getEffectivePower()).isEqualTo(2);
        assertThat(champion.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, champion))
                .doesNotContain(CardSubtype.BEAR)
                .contains(CardSubtype.HUMAN, CardSubtype.BERSERKER);
    }

    @Test
    @DisplayName("A second activation is forbidden while the first is still on the stack")
    void limitAppliesBeforeResolution() {
        Permanent champion = addReadyChampion(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");

        harness.passBothPriorities();
        assertThat(champion.getEffectivePower()).isEqualTo(5);
        assertThat(champion.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Champion can activate during an opponent's turn")
    void activatesWithoutTapOrSummoningRestrictionOnOpponentsTurn() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new UrsineChampion());
        champion.setSummoningSick(true);
        champion.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addAbilityMana(player1);
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(champion.isTapped()).isTrue();
        assertThat(champion.getEffectivePower()).isEqualTo(5);
        assertThat(champion.getEffectiveToughness()).isEqualTo(5);
        assertThat(gqs.effectiveCreatureSubtypes(gd, champion))
                .containsExactlyInAnyOrder(CardSubtype.BEAR, CardSubtype.BERSERKER);
    }

    @Test
    @DisplayName("Each Champion has its own activation allowance")
    void activationLimitIsPerPermanent() {
        Permanent first = addReadyChampion(player1);
        Permanent second = addReadyChampion(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(first.getEffectivePower()).isEqualTo(5);
        assertThat(second.getEffectivePower()).isEqualTo(5);
        assertThat(gqs.effectiveCreatureSubtypes(gd, first))
                .containsExactlyInAnyOrder(CardSubtype.BEAR, CardSubtype.BERSERKER);
        assertThat(gqs.effectiveCreatureSubtypes(gd, second))
                .containsExactlyInAnyOrder(CardSubtype.BEAR, CardSubtype.BERSERKER);
    }

    private Permanent addReadyChampion(Player player) {
        return addCreatureReady(player, new UrsineChampion());
    }

    private void addAbilityMana(Player player) {
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 5);
    }
}
