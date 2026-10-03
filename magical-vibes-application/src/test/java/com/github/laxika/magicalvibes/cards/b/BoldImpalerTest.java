package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoldImpaler.class})
class BoldImpalerTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 when its ability resolves")
    void pumpsWhenActivated() {
        Permanent impaler = addCreatureReady(player1, new BoldImpaler());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, battlefieldIndex(impaler), null, null);
        harness.passBothPriorities();

        assertThat(impaler.getPowerModifier()).isEqualTo(2);
        assertThat(impaler.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Activations stack")
    void pumpStacks() {
        Permanent impaler = addCreatureReady(player1, new BoldImpaler());
        harness.addMana(player1, ManaColor.RED, 6);

        harness.activateAbility(player1, battlefieldIndex(impaler), null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, battlefieldIndex(impaler), null, null);
        harness.passBothPriorities();

        assertThat(impaler.getPowerModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent impaler = addCreatureReady(player1, new BoldImpaler());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, battlefieldIndex(impaler), null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(impaler.getPowerModifier()).isEqualTo(0);
        assertThat(impaler.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent impaler = harness.addToBattlefieldAndReturn(player1, new BoldImpaler());
        impaler.setSummoningSick(true);
        impaler.setTapped(true);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(impaler), null, null);
        harness.passBothPriorities();

        assertThat(impaler.getPowerModifier()).isEqualTo(2);
        assertThat(impaler.getToughnessModifier()).isZero();
        assertThat(impaler.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot pay the red portion with colorless mana")
    void requiresRedMana() {
        Permanent impaler = addCreatureReady(player1, new BoldImpaler());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(impaler), null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(impaler.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Multiple pending activations boost only their source")
    void pendingActivationsBoostOnlyTheirSource() {
        Permanent impaler = addCreatureReady(player1, new BoldImpaler());
        Permanent other = addCreatureReady(player1, new BoldImpaler());
        harness.addMana(player1, ManaColor.RED, 6);

        harness.activateAbility(player1, battlefieldIndex(impaler), null, null);
        harness.activateAbility(player1, battlefieldIndex(impaler), null, null);

        assertThat(impaler.getPowerModifier()).isZero();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(impaler.getPowerModifier()).isEqualTo(4);
        assertThat(impaler.getToughnessModifier()).isZero();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
