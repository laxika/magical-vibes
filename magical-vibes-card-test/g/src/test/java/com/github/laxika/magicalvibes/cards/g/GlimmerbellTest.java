package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Glimmerbell.class})
class GlimmerbellTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1}{U} untaps Glimmerbell")
    void activatedAbilityUntapsSelf() {
        Permanent glimmerbell = addCreatureReady(player1, new Glimmerbell());
        glimmerbell.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(glimmerbell.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Untapping uses the stack and affects only the activating Glimmerbell")
    void untapsOnlySourceOnResolution() {
        Permanent other = addCreatureReady(player1, new Glimmerbell());
        Permanent source = addCreatureReady(player1, new Glimmerbell());
        Permanent opposing = addCreatureReady(player2, new Glimmerbell());
        other.tap();
        source.tap();
        opposing.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 1, null, null);

        assertThat(source.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        assertThat(source.isTapped()).isFalse();
        assertThat(other.isTapped()).isTrue();
        assertThat(opposing.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A summoning-sick Glimmerbell can activate its untap ability")
    void canUntapWhileSummoningSick() {
        Permanent glimmerbell = harness.addToBattlefieldAndReturn(player1, new Glimmerbell());
        glimmerbell.setSummoningSick(true);
        glimmerbell.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(glimmerbell.isTapped()).isFalse();
        assertThat(glimmerbell.isSummoningSick()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An untapped Glimmerbell can activate its ability repeatedly")
    void canActivateRepeatedlyWhileUntapped() {
        Permanent glimmerbell = addCreatureReady(player1, new Glimmerbell());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(glimmerbell.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
