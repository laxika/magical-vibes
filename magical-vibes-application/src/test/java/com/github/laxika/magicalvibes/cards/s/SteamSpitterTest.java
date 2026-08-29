package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SteamSpitterTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives +1/+0 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent spider = addReadySteamSpitter(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(spider.getPowerModifier()).isEqualTo(1);
        assertThat(spider.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ability can be activated repeatedly for a cumulative boost")
    void repeatedActivationsStack() {
        Permanent spider = addReadySteamSpitter(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(spider.getPowerModifier()).isEqualTo(2);
        assertThat(spider.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate the ability without red mana")
    void cannotActivateWithoutRedMana() {
        addReadySteamSpitter(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent spider = addReadySteamSpitter(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(spider.getPowerModifier()).isEqualTo(0);
        assertThat(spider.getToughnessModifier()).isEqualTo(0);
    }

    private Permanent addReadySteamSpitter(Player player) {
        Permanent perm = new Permanent(new SteamSpitter());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
