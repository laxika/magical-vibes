package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GnarledEffigyTest extends BaseCardTest {

    // ===== Puts a -1/-1 counter on target creature =====

    @Test
    @DisplayName("Puts a -1/-1 counter on target creature")
    void putsMinusCounterOnTargetCreature() {
        Permanent effigy = addReadyEffigy(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addToBattlefield(player2, new GrizzlyBears());

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(effigy);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, index, null, targetId);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
        assertThat(effigy.isTapped()).isTrue();
    }

    // ===== -1/-1 counter can kill a 1/1 =====

    @Test
    @DisplayName("A -1/-1 counter reduces a 1/1 to 0 toughness and it dies")
    void killsOneToughnessCreature() {
        Permanent effigy = addReadyEffigy(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addToBattlefield(player2, new LlanowarElves());

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(effigy);
        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.activateAbility(player1, index, null, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if target creature leaves before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent effigy = addReadyEffigy(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addToBattlefield(player2, new GrizzlyBears());

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(effigy);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, index, null, targetId);

        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    // ===== Helpers =====

    private Permanent addReadyEffigy(Player player) {
        Permanent perm = new Permanent(new GnarledEffigy());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
