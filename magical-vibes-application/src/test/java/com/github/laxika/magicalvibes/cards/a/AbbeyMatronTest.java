package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AbbeyMatron.class})
class AbbeyMatronTest extends BaseCardTest {

    @Test
    @DisplayName("Ability grants +0/+3 and taps the matron")
    void abilityGrantsToughness() {
        Permanent matron = addCreatureReady(player1, new AbbeyMatron());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, matron)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, matron)).isEqualTo(6);
        assertThat(matron.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent matron = addCreatureReady(player1, new AbbeyMatron());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, matron)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, matron)).isEqualTo(3);
    }

    @Test
    void abilityRequiresWhiteMana() {
        Permanent matron = addCreatureReady(player1, new AbbeyMatron());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(matron.isTapped()).isFalse();
        assertThat(gqs.getEffectiveToughness(gd, matron)).isEqualTo(3);
    }

    @Test
    void abilityRequiresAReadyCreature() {
        Permanent matron = new Permanent(new AbbeyMatron());
        gd.playerBattlefields.get(player1.getId()).add(matron);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(matron.isTapped()).isFalse();
        assertThat(gqs.getEffectiveToughness(gd, matron)).isEqualTo(3);
    }
}
