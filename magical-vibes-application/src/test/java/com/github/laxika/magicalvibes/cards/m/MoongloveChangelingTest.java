package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(MoongloveChangeling.class)
class MoongloveChangelingTest extends BaseCardTest {

    @Test
    @DisplayName("{B}: gains deathtouch until end of turn")
    void grantsDeathtouch() {
        Permanent changeling = addCreatureReady(player1, new MoongloveChangeling());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(changeling.getGrantedKeywords()).contains(Keyword.DEATHTOUCH);
    }

    @Test
    @DisplayName("Activation consumes exactly one black mana")
    void activationConsumesBlackMana() {
        addCreatureReady(player1, new MoongloveChangeling());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate without black mana")
    void cannotActivateWithoutBlackMana() {
        addCreatureReady(player1, new MoongloveChangeling());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Deathtouch wears off at end of turn")
    void deathtouchWearsOffAtEndOfTurn() {
        Permanent changeling = addCreatureReady(player1, new MoongloveChangeling());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(changeling.getGrantedKeywords()).contains(Keyword.DEATHTOUCH);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(changeling.getGrantedKeywords()).doesNotContain(Keyword.DEATHTOUCH);
    }
}
