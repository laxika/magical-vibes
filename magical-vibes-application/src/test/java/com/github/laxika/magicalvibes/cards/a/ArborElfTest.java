package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArborElfTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps a tapped Forest and taps Arbor Elf as the cost")
    void untapsTappedForest() {
        Permanent elf = addReadyElf(player1);
        Permanent forest = addPermanent(player1, new Forest());
        forest.tap();

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(elf.isTapped()).isTrue();
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can untap a Forest an opponent controls")
    void canTargetOpponentForest() {
        addReadyElf(player1);
        Permanent forest = addPermanent(player2, new Forest());
        forest.tap();

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-Forest land")
    void cannotTargetNonForest() {
        addReadyElf(player1);
        Permanent island = addPermanent(player1, new Island());
        island.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target does not match the required predicate");
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWithSummoningSickness() {
        Permanent elf = new Permanent(new ArborElf());
        gd.playerBattlefields.get(player1.getId()).add(elf);
        Permanent forest = addPermanent(player1, new Forest());
        forest.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    private Permanent addReadyElf(Player player) {
        Permanent perm = new Permanent(new ArborElf());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
