package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
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

@CardUsed({MasakoTheHumorless.class, IsamaruHoundOfKonda.class})
class MasakoTheHumorlessTest extends BaseCardTest {

    @Test
    @DisplayName("Masako lets a tapped creature you control block")
    void tappedCreatureCanBlockWithMasako() {
        harness.addToBattlefield(player2, new MasakoTheHumorless());
        Permanent blocker = addCreatureReady(player2, new IsamaruHoundOfKonda());
        blocker.tap();
        Permanent attacker = addAttacker(player1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Masako can block while tapped")
    void masakoCanBlockWhileTapped() {
        Permanent masako = addCreatureReady(player2, new MasakoTheHumorless());
        masako.tap();
        Permanent attacker = addAttacker(player1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(masako),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(masako.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A tapped creature cannot block without Masako")
    void tappedCreatureCannotBlockWithoutMasako() {
        Permanent blocker = addCreatureReady(player2, new IsamaruHoundOfKonda());
        blocker.tap();
        Permanent attacker = addAttacker(player1);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Masako does not let an opponent's tapped creature block")
    void tappedOpponentCreatureCannotBlockWithMasako() {
        harness.addToBattlefield(player1, new MasakoTheHumorless());
        Permanent blocker = addCreatureReady(player2, new IsamaruHoundOfKonda());
        blocker.tap();
        Permanent attacker = addAttacker(player1);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    private Permanent addAttacker(Player player) {
        Permanent creature = addCreatureReady(player, new IsamaruHoundOfKonda());
        creature.setAttacking(true);
        return creature;
    }
}
