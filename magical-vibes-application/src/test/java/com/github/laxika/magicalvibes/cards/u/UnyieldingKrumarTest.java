package com.github.laxika.magicalvibes.cards.u;

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

class UnyieldingKrumarTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability grants first strike until end of turn")
    void resolvingAbilityGrantsFirstStrike() {
        Permanent krumar = addKrumarReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, krumar, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Granted first strike wears off at end of turn")
    void firstStrikeWearsOff() {
        Permanent krumar = addKrumarReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, krumar, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Ability requires one generic and one white mana")
    void requiresOneGenericAndOneWhiteMana() {
        addKrumarReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addKrumarReady(Player player) {
        Permanent permanent = new Permanent(new UnyieldingKrumar());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
