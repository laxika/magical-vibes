package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.MeliraSylvokOutcast;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DevotedDruid.class, MeliraSylvokOutcast.class})
class DevotedDruidTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Devoted Druid produces one green mana")
    void tappingProducesGreenMana() {
        Permanent druid = addCreatureReady(player1, new DevotedDruid());

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(druid.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untap ability untaps the Druid and puts a -1/-1 counter on it as a cost")
    void untapAbilityUntapsAndAddsCounter() {
        Permanent druid = addCreatureReady(player1, new DevotedDruid());
        druid.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);

        // Cost is paid immediately on activation.
        assertThat(druid.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(druid.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(druid.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untap ability can be activated during an opponent's turn")
    void untapAbilityCanBeActivatedDuringOpponentsTurn() {
        Permanent druid = addCreatureReady(player1, new DevotedDruid());
        druid.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(druid.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(druid.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untapping then re-tapping lets Devoted Druid produce additional mana")
    void untapEnablesAdditionalMana() {
        Permanent druid = addCreatureReady(player1, new DevotedDruid());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        gs.tapPermanent(gd, player1, 0);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(druid.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The mana ability resolves immediately without using the stack")
    void manaAbilityDoesNotUseStack() {
        addCreatureReady(player1, new DevotedDruid());

        harness.tapPermanent(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Summoning sickness prevents activating the tap mana ability")
    void summoningSicknessPreventsManaAbility() {
        Permanent druid = addCreatureReady(player1, new DevotedDruid());
        druid.setSummoningSick(true);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(druid.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("The untap ability can be activated while untapped and summoning sick")
    void untapAbilityDoesNotRequireTapOrHaste() {
        Permanent druid = addCreatureReady(player1, new DevotedDruid());
        druid.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(druid.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(druid.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Devoted Druid");
    }

    @Test
    @DisplayName("A second counter kills the Druid before its untap ability resolves")
    void lethalCounterCostKillsBeforeResolution() {
        Permanent druid = addCreatureReady(player1, new DevotedDruid());
        harness.tapPermanent(player1, 0);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.tapPermanent(player1, 0);

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Devoted Druid");
        harness.assertInGraveyard(player1, "Devoted Druid");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        harness.passBothPriorities();
        assertThat(druid.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({DevotedDruid.class, MeliraSylvokOutcast.class})
    @DisplayName("Melira prevents paying the untap ability's counter cost")
    void cannotActivateWhenMinusCountersAreProhibited() {
        Permanent druid = addCreatureReady(player1, new DevotedDruid());
        addCreatureReady(player1, new MeliraSylvokOutcast());
        harness.tapPermanent(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(druid.isTapped()).isTrue();
        assertThat(druid.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
