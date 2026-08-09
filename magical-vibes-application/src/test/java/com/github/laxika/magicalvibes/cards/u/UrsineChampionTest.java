package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
                .hasMessageContaining("no more than 1 times each turn");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(champion.getEffectivePower()).isEqualTo(2);
        assertThat(champion.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, champion))
                .doesNotContain(CardSubtype.BEAR)
                .contains(CardSubtype.HUMAN, CardSubtype.BERSERKER);
    }

    private Permanent addReadyChampion(Player player) {
        Permanent permanent = new Permanent(new UrsineChampion());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addAbilityMana(Player player) {
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 5);
    }
}
