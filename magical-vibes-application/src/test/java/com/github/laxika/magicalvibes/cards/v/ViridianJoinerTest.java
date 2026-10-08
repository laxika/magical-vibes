package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.p.PredatorsStrike;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ViridianJoiner.class, PredatorsStrike.class})
class ViridianJoinerTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability produces green mana equal to its power")
    void tapProducesGreenManaEqualToPower() {
        addCreatureReady(player1, new ViridianJoiner());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tap ability uses the creature's current power")
    void tapUsesCurrentPower() {
        Permanent joiner = addCreatureReady(player1, new ViridianJoiner());
        joiner.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    @DisplayName("Tap ability taps the Joiner and cannot be activated again while tapped")
    void tapAbilityRequiresUntappedJoiner() {
        Permanent joiner = addCreatureReady(player1, new ViridianJoiner());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(joiner.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mana ability resolves immediately without using the stack")
    void manaAbilityResolvesImmediately() {
        addCreatureReady(player1, new ViridianJoiner());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Zero power produces no mana but still pays the tap cost")
    void zeroPowerProducesNoMana() {
        Permanent joiner = addCreatureReady(player1, new ViridianJoiner());
        joiner.setPowerModifier(-1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(joiner.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Negative power produces no mana and does not remove existing mana")
    void negativePowerProducesNoMana() {
        Permanent joiner = addCreatureReady(player1, new ViridianJoiner());
        joiner.setPowerModifier(-2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(joiner.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Summoning sickness prevents activating the tap ability")
    void summoningSicknessPreventsActivation() {
        Permanent joiner = harness.addToBattlefieldAndReturn(player1, new ViridianJoiner());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(joiner.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Mana production includes a resolved temporary power boost")
    void temporaryPowerBoostIncreasesManaProduction() {
        Permanent joiner = addCreatureReady(player1, new ViridianJoiner());
        harness.setHand(player1, List.of(new PredatorsStrike()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, joiner.getId());
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(4);
        assertThat(joiner.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
