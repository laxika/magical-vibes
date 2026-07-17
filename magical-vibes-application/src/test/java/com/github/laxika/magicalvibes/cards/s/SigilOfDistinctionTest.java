package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SigilOfDistinctionTest extends BaseCardTest {

    // ===== Enters with X charge counters =====

    @Test
    @DisplayName("Casting Sigil of Distinction with X=3 enters with 3 charge counters")
    void entersWith3ChargeCounters() {
        harness.setHand(player1, List.of(new SigilOfDistinction()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent sigil = findSigil(player1);
        assertThat(sigil).isNotNull();
        assertThat(sigil.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    // ===== Static boost scales with charge counters =====

    @Test
    @DisplayName("Equipped creature gets +1/+1 for each charge counter")
    void equippedCreatureBoostedPerChargeCounter() {
        Permanent bears = addReadyCreature(player1);
        Permanent sigil = new Permanent(new SigilOfDistinction());
        sigil.setCounterCount(CounterType.CHARGE, 3);
        sigil.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(sigil);

        // 2/2 base + 3 charge counters = 5/5
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("Equipped creature gets no boost with zero charge counters")
    void noBoostWithZeroChargeCounters() {
        Permanent bears = addReadyCreature(player1);
        Permanent sigil = new Permanent(new SigilOfDistinction());
        sigil.setCounterCount(CounterType.CHARGE, 0);
        sigil.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(sigil);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    // ===== Equip cost removes a charge counter =====

    @Test
    @DisplayName("Equipping removes a charge counter and attaches to the target")
    void equipRemovesChargeCounterAndAttaches() {
        Permanent sigil = new Permanent(new SigilOfDistinction());
        sigil.setSummoningSick(false);
        sigil.setCounterCount(CounterType.CHARGE, 3);
        gd.playerBattlefields.get(player1.getId()).add(sigil);
        Permanent bears = addReadyCreature(player1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(sigil.getAttachedTo()).isEqualTo(bears.getId());
        // One charge counter spent to equip: 3 -> 2
        assertThat(sigil.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        // 2/2 base + 2 remaining charge counters = 4/4
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot equip with no charge counters to remove")
    void cannotEquipWithoutChargeCounters() {
        Permanent sigil = new Permanent(new SigilOfDistinction());
        sigil.setSummoningSick(false);
        sigil.setCounterCount(CounterType.CHARGE, 0);
        gd.playerBattlefields.get(player1.getId()).add(sigil);
        Permanent bears = addReadyCreature(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Illegal target =====

    @Test
    @DisplayName("Cannot equip a creature an opponent controls")
    void cannotEquipOpponentCreature() {
        Permanent sigil = new Permanent(new SigilOfDistinction());
        sigil.setSummoningSick(false);
        sigil.setCounterCount(CounterType.CHARGE, 3);
        gd.playerBattlefields.get(player1.getId()).add(sigil);

        Permanent opponentBears = new Permanent(new GrizzlyBears());
        opponentBears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(opponentBears);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent findSigil(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Sigil of Distinction"))
                .findFirst().orElse(null);
    }
}
