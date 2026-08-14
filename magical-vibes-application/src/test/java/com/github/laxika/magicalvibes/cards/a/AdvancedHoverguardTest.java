package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdvancedHoverguardTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability grants shroud until end of turn")
    void resolvingAbilityGrantsShroud() {
        Permanent hoverguard = addHoverguardReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hoverguard, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Shroud wears off at end of turn")
    void shroudWearsOff() {
        Permanent hoverguard = addHoverguardReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hoverguard, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("The ability requires one blue mana")
    void requiresBlueMana() {
        addHoverguardReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addHoverguardReady(Player player) {
        Permanent perm = new Permanent(new AdvancedHoverguard());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
