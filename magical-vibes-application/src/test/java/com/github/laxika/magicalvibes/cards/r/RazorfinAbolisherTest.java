package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RazorfinAbolisherTest extends BaseCardTest {

    // ===== Resolving =====

    @Test
    @DisplayName("Ability returns target creature with a counter to its owner's hand")
    void abilityReturnsCreatureWithCounter() {
        Permanent abolisher = addReadyAbolisher(player1);
        Permanent target = addBearsWithCounter(player2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(abolisher.isTapped()).isTrue();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Illegal targets =====

    @Test
    @DisplayName("Ability cannot target a creature without a counter")
    void cannotTargetCreatureWithoutCounter() {
        addReadyAbolisher(player1);
        Permanent target = addReadyBears(player2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Cannot activate =====

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent abolisher = addReadyAbolisher(player1);
        abolisher.tap();
        Permanent target = addBearsWithCounter(player2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    private Permanent addReadyAbolisher(Player player) {
        Permanent perm = new Permanent(new RazorfinAbolisher());
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
