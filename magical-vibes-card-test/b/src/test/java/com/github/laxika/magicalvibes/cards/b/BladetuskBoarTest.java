package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CanyonMinotaur;
import com.github.laxika.magicalvibes.cards.p.PhyrexianHulk;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BladetuskBoar.class, CanyonMinotaur.class, PhyrexianHulk.class, WalkingCorpse.class})
class BladetuskBoarTest extends BaseCardTest {

    @Test
    void nonRedNonartifactCreatureCannotBlock() {
        Permanent boar = addCreatureReady(player1, new BladetuskBoar());
        addCreatureReady(player2, new WalkingCorpse());
        boar.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("intimidate");
    }

    @Test
    void redNonartifactCreatureCanBlock() {
        Permanent boar = addCreatureReady(player1, new BladetuskBoar());
        Permanent blocker = addCreatureReady(player2, new CanyonMinotaur());
        boar.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void colorlessArtifactCreatureCanBlock() {
        Permanent boar = addCreatureReady(player1, new BladetuskBoar());
        Permanent blocker = addCreatureReady(player2, new PhyrexianHulk());
        boar.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void intimidateDoesNotRestrictBoarsOwnBlocking() {
        Permanent attacker = addCreatureReady(player1, new WalkingCorpse());
        Permanent boar = addCreatureReady(player2, new BladetuskBoar());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(boar.isBlocking()).isTrue();
    }
}
