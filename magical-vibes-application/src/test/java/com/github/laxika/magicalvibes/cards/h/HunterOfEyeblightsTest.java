package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HunterOfEyeblightsTest extends BaseCardTest {

    // ===== ETB: put a +1/+1 counter on target creature you don't control =====

    @Test
    @DisplayName("ETB puts a +1/+1 counter on target creature you don't control")
    void etbPutsCounterOnOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new HunterOfEyeblights()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.getGameService().playCard(gd, player1, 0, 0, bearsId, null);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB cannot target a creature you control")
    void etbCannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new HunterOfEyeblights()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, bearsId, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Activated ability: {2}{B}, {T}: Destroy target creature with a counter on it =====

    @Test
    @DisplayName("Ability destroys target creature that has a counter")
    void abilityDestroysCreatureWithCounter() {
        Permanent hunter = addReadyHunter(player1);
        Permanent target = addBearsWithCounter(player2);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(hunter.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Ability cannot target a creature without a counter")
    void abilityCannotTargetCreatureWithoutCounter() {
        addReadyHunter(player1);
        Permanent target = addReadyBears(player2);
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyHunter(Player player) {
        Permanent perm = new Permanent(new HunterOfEyeblights());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyBears(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addBearsWithCounter(Player player) {
        Permanent perm = addReadyBears(player);
        perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        return perm;
    }
}
