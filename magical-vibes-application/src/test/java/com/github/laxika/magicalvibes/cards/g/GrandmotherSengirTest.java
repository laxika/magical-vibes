package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BeastWalkers;
import com.github.laxika.magicalvibes.cards.d.DwarvenTrader;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrandmotherSengir.class, BeastWalkers.class, DwarvenTrader.class})
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
        Permanent token = addCreatureReady(player2, new DwarvenTrader());

        harness.activateAbility(player1, 0, null, token.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(token);
    }

    @Test
    @DisplayName("Can target a creature controlled by its controller")
    void weakensOwnCreature() {
        setupSengir();
        Permanent sengir = findPermanent(player1, "Grandmother Sengir");

        harness.activateAbility(player1, 0, null, sengir.getId());
        harness.passBothPriorities();

        assertThat(sengir.getPowerModifier()).isEqualTo(-1);
        assertThat(sengir.getToughnessModifier()).isEqualTo(-1);
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
        addCreatureReady(player1, new GrandmotherSengir());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
    }

    private Permanent addBear() {
        return addCreatureReady(player2, new BeastWalkers());
    }
}
