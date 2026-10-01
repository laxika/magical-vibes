package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DevotedDruid.class})
class DevotedDruidTest extends BaseCardTest {

    // ===== Mana ability =====

    @Test
    @DisplayName("Tapping Devoted Druid produces one green mana")
    void tappingProducesGreenMana() {
        Permanent druid = addCreatureReady(player1, new DevotedDruid());

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(druid.isTapped()).isTrue();
    }

    // ===== Untap ability =====

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
}
