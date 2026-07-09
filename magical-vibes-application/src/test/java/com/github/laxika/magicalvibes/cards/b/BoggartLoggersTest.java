package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BoggartLoggersTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing to destroy a target Treefolk removes both permanents")
    void destroysTargetTreefolk() {
        Permanent loggers = addReadyLoggers(player1);
        Permanent treefolk = addReady(player2, new BlackPoplarShaman());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, treefolk.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(loggers);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Boggart Loggers"));
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(treefolk);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Black Poplar Shaman"));
    }

    @Test
    @DisplayName("Can destroy a target Forest")
    void destroysTargetForest() {
        addReadyLoggers(player1);
        Permanent forest = addReady(player2, new Forest());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(forest);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Cannot target a non-Treefolk creature")
    void cannotTargetNonTreefolkCreature() {
        addReadyLoggers(player1);
        Permanent bears = addReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-Forest land")
    void cannotTargetNonForestLand() {
        addReadyLoggers(player1);
        Permanent island = addReady(player2, new Island());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addReadyLoggers(player1);
        Permanent treefolk = addReady(player2, new BlackPoplarShaman());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, treefolk.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyLoggers(Player player) {
        return addReady(player, new BoggartLoggers());
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
