package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DrossCrocodile;
import com.github.laxika.magicalvibes.cards.s.SkyhunterProwler;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Arachnoid.class, DrossCrocodile.class, SkyhunterProwler.class})
class ArachnoidTest extends BaseCardTest {

    @Test
    @DisplayName("Reach lets Arachnoid block a creature with flying")
    void reachCanBlockFlyer() {
        Permanent flyer = addCreatureReady(player1, new SkyhunterProwler());
        flyer.setAttacking(true);
        Permanent arachnoid = addCreatureReady(player2, new Arachnoid());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, arachnoid), indexOf(player1, flyer))));

        assertThat(arachnoid.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature without flying or reach cannot block the flyer")
    void nonReachCannotBlockFlyer() {
        Permanent flyer = addCreatureReady(player1, new SkyhunterProwler());
        flyer.setAttacking(true);
        Permanent crocodile = addCreatureReady(player2, new DrossCrocodile());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, crocodile), indexOf(player1, flyer)))))
                .isInstanceOf(IllegalStateException.class);
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
