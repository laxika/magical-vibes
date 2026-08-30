package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfAir;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarblePriest.class, WallOfAir.class, GrizzlyBears.class})
class MarblePriestTest extends BaseCardTest {

    @Test
    @DisplayName("All able Walls must block Marble Priest, but other creatures are not forced")
    void allAbleWallsMustBlockButOtherCreaturesAreNotForced() {
        Permanent priest = attackingCreature(new MarblePriest());
        gd.playerBattlefields.get(player1.getId()).add(priest);

        gd.playerBattlefields.get(player2.getId()).add(readyCreature(new WallOfAir()));
        gd.playerBattlefields.get(player2.getId()).add(readyCreature(new GrizzlyBears()));

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Combat damage from Walls is prevented, but combat damage from other creatures is not")
    void preventsCombatDamageFromWallsOnly() {
        Permanent priest = attackingCreature(new MarblePriest());
        gd.playerBattlefields.get(player1.getId()).add(priest);

        Permanent wall = readyCreature(new WallOfAir());
        Permanent bears = readyCreature(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(wall);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(priest.getMarkedDamage()).isEqualTo(2);
        assertThat(wall.getMarkedDamage()).isEqualTo(3);
    }

    private Permanent attackingCreature(Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        return permanent;
    }

    private Permanent readyCreature(Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
