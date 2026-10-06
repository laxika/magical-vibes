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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    @DisplayName("Weakening applies only when the activated ability resolves")
    void weakeningWaitsForResolution() {
        setupSengir();
        Permanent target = addBear();

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("A tapped Grandmother Sengir cannot activate its ability")
    void cannotActivateWhileTapped() {
        setupSengir();
        Permanent source = findPermanent(player1, "Grandmother Sengir");
        source.tap();
        Permanent target = addBear();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        assertThat(gd.stack).isEmpty();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new GrandmotherSengir());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        Permanent target = addBear();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Grandmother Sengir").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activation requires black mana as well as the generic payment")
    void cannotActivateWithoutBlackMana() {
        Permanent source = addCreatureReady(player1, new GrandmotherSengir());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        Permanent target = addBear();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");

        assertThat(gd.stack).isEmpty();
        assertThat(source.isTapped()).isFalse();
    }

    @Test
    @DisplayName("One black mana alone does not pay the activation cost")
    void cannotActivateWithoutGenericPayment() {
        Permanent source = addCreatureReady(player1, new GrandmotherSengir());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        Permanent target = addBear();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");

        assertThat(gd.stack).isEmpty();
        assertThat(source.isTapped()).isFalse();
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
