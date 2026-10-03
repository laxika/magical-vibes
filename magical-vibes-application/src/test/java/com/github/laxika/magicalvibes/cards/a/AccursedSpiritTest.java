package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BrindleBoar;
import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.cards.s.SliverConstruct;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AccursedSpirit.class, BrindleBoar.class, ChildOfNight.class, SliverConstruct.class})
class AccursedSpiritTest extends BaseCardTest {

    @Test
    void greenNonartifactCreatureCannotBlock() {
        Permanent spirit = addCreatureReady(player1, new AccursedSpirit());
        addCreatureReady(player2, new BrindleBoar());
        spirit.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("intimidate");
    }

    @Test
    void blackCreatureCanBlock() {
        Permanent spirit = addCreatureReady(player1, new AccursedSpirit());
        Permanent blocker = addCreatureReady(player2, new ChildOfNight());
        spirit.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void colorlessArtifactCreatureCanBlock() {
        Permanent spirit = addCreatureReady(player1, new AccursedSpirit());
        Permanent blocker = addCreatureReady(player2, new SliverConstruct());
        spirit.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void intimidateDoesNotRestrictSpiritsOwnBlocking() {
        Permanent attacker = addCreatureReady(player1, new BrindleBoar());
        Permanent spirit = addCreatureReady(player2, new AccursedSpirit());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spirit.isBlocking()).isTrue();
    }
}
