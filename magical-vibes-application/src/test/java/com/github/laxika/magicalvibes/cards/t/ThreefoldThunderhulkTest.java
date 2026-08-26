package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThreefoldThunderhulk.class, Spellbook.class})
class ThreefoldThunderhulkTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three counters and creates Gnomes equal to its power")
    void entersWithCountersAndCreatesGnomes() {
        harness.setHand(player1, List.of(new ThreefoldThunderhulk()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent hulk = findPermanent(player1, "Threefold Thunderhulk");
        assertThat(hulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(findPermanents(player1, "Gnome")).hasSize(3);
    }

    @Test
    @DisplayName("Attack trigger creates Gnomes equal to current power")
    void attackCreatesGnomesEqualToCurrentPower() {
        Permanent hulk = addReadyHulk(player1, 4);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Gnome")).hasSize(4);
        assertThat(hulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Sacrificing another artifact puts a +1/+1 counter on the Hulk")
    void sacrificeArtifactAddsCounter() {
        Permanent hulk = addReadyHulk(player1, 3);
        harness.addToBattlefield(player1, new Spellbook());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        harness.assertNotOnBattlefield(player1, "Spellbook");
    }

    @Test
    @DisplayName("Cannot sacrifice the Hulk itself when no other artifact is available")
    void cannotActivateWithoutAnotherArtifact() {
        addReadyHulk(player1, 3);

        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: another artifact");
    }

    private Permanent addReadyHulk(Player player, int counters) {
        Permanent hulk = new Permanent(new ThreefoldThunderhulk());
        hulk.setSummoningSick(false);
        hulk.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        gd.playerBattlefields.get(player.getId()).add(hulk);
        return hulk;
    }
}
