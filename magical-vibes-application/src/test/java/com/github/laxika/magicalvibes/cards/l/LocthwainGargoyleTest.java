package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(LocthwainGargoyle.class)
class LocthwainGargoyleTest extends BaseCardTest {

    @Test
    void activatedAbilityBoostsAndGrantsFlying() {
        Permanent gargoyle = addReadyGargoyle(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gargoyle.getPowerModifier()).isEqualTo(2);
        assertThat(gargoyle.getToughnessModifier()).isZero();
        assertThat(gargoyle.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    void repeatedActivationsStack() {
        Permanent gargoyle = addReadyGargoyle(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gargoyle.getPowerModifier()).isEqualTo(4);
        assertThat(gargoyle.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    void boostAndFlyingWearOffAtEndOfTurn() {
        Permanent gargoyle = addReadyGargoyle(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gargoyle.getPowerModifier()).isZero();
        assertThat(gargoyle.getToughnessModifier()).isZero();
        assertThat(gargoyle.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    void cannotActivateWithoutEnoughMana() {
        addReadyGargoyle(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addReadyGargoyle(Player player) {
        Permanent gargoyle = new Permanent(new LocthwainGargoyle());
        gargoyle.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(gargoyle);
        return gargoyle;
    }
}
