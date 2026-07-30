package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GrandmotherSengirTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -1/-1 until end of turn")
    void weakensTargetCreature() {
        setupSengir();
        Permanent bear = addBear();

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(-1);
        assertThat(bear.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Kills a 1/1 creature via state-based actions")
    void killsOneToughnessCreature() {
        setupSengir();
        Permanent token = new Permanent(new GrizzlyBears());
        token.setSummoningSick(false);
        token.setToughnessModifier(-1);
        token.setPowerModifier(-1);
        gd.playerBattlefields.get(player2.getId()).add(token);

        harness.activateAbility(player1, 0, null, token.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(token);
    }

    @Test
    @DisplayName("Weakening wears off at cleanup")
    void weakeningWearsOff() {
        setupSengir();
        Permanent bear = addBear();

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Taps Grandmother Sengir when activated")
    void tapsOnActivation() {
        setupSengir();
        Permanent bear = addBear();

        harness.activateAbility(player1, 0, null, bear.getId());

        assertThat(findPermanent(player1, "Grandmother Sengir").isTapped()).isTrue();
    }

    private void setupSengir() {
        harness.addToBattlefield(player1, new GrandmotherSengir());
        findPermanent(player1, "Grandmother Sengir").setSummoningSick(false);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
    }

    private Permanent addBear() {
        Permanent bear = new Permanent(new GrizzlyBears());
        bear.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bear);
        return bear;
    }
}
