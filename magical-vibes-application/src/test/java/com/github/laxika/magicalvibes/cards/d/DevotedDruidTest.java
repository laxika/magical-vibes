package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DevotedDruidTest extends BaseCardTest {

    // ===== Mana ability =====

    @Test
    @DisplayName("Tapping Devoted Druid produces one green mana")
    void tappingProducesGreenMana() {
        Permanent druid = addReadyDruid(player1);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(druid.isTapped()).isTrue();
    }

    // ===== Untap ability =====

    @Test
    @DisplayName("Untap ability untaps the Druid and puts a -1/-1 counter on it as a cost")
    void untapAbilityUntapsAndAddsCounter() {
        Permanent druid = addReadyDruid(player1);
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
    @DisplayName("Untapping then re-tapping lets Devoted Druid produce additional mana")
    void untapEnablesAdditionalMana() {
        Permanent druid = addReadyDruid(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        gs.tapPermanent(gd, player1, 0);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(druid.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    // ===== Helpers =====

    private Permanent addReadyDruid(Player player) {
        Permanent druid = new Permanent(new DevotedDruid());
        druid.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(druid);
        return druid;
    }
}
