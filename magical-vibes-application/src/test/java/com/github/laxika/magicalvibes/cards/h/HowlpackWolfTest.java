package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AfflictedDeserter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YoungWolf;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HowlpackWolfTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot block without another Wolf or Werewolf")
    void cannotBlockWithoutAnotherWolfOrWerewolf() {
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new HowlpackWolf());

        declareAttackers(player2, List.of(0));

        prepareDeclareBlockers(player2);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can block when controlling another Wolf")
    void canBlockWithAnotherWolf() {
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new HowlpackWolf());
        addCreatureReady(player1, new YoungWolf());

        declareAttackers(player2, List.of(0));

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Can block when controlling another Werewolf")
    void canBlockWithAnotherWerewolf() {
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new HowlpackWolf());
        Permanent werewolf = addCreatureReady(player1, new AfflictedDeserter());
        werewolf.setCard(werewolf.getOriginalCard().getBackFaceCard());
        werewolf.setTransformed(true);

        declareAttackers(player2, List.of(0));

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Can attack even without another Wolf or Werewolf")
    void canAttackWithoutAnotherWolfOrWerewolf() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new HowlpackWolf());

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }
}
