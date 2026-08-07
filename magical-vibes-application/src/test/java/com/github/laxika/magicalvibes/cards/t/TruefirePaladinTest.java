package com.github.laxika.magicalvibes.cards.t;

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

class TruefirePaladinTest extends BaseCardTest {

    @Test
    @DisplayName("First ability gives +2/+0 until end of turn")
    void pumpAbility() {
        Permanent paladin = addPaladin(player1);
        addRedWhite(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, paladin)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, paladin)).isEqualTo(2);
    }

    @Test
    @DisplayName("The +2/+0 boost wears off at end of turn")
    void pumpWearsOff() {
        Permanent paladin = addPaladin(player1);
        addRedWhite(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, paladin)).isEqualTo(2);
    }

    @Test
    @DisplayName("Second ability grants first strike until end of turn")
    void firstStrikeAbility() {
        Permanent paladin = addPaladin(player1);
        addRedWhite(player1);

        assertThat(gqs.hasKeyword(gd, paladin, Keyword.FIRST_STRIKE)).isFalse();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, paladin, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Granted first strike wears off at end of turn")
    void firstStrikeWearsOff() {
        Permanent paladin = addPaladin(player1);
        addRedWhite(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, paladin, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Both abilities can be activated in the same turn")
    void bothAbilitiesStack() {
        Permanent paladin = addPaladin(player1);
        addRedWhite(player1);
        addRedWhite(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, paladin)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without both red and white mana")
    void cannotActivateWithoutMana() {
        addPaladin(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private void addRedWhite(Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
    }

    private Permanent addPaladin(Player player) {
        Permanent perm = new Permanent(new TruefirePaladin());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
