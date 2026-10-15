package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AfflictedDeserter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YoungWolf;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HowlpackWolf.class, GrizzlyBears.class, YoungWolf.class, AfflictedDeserter.class})
class HowlpackWolfTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot block without another Wolf or Werewolf")
    void cannotBlockWithoutAnotherWolfOrWerewolf() {
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new HowlpackWolf());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can block when controlling another Wolf")
    void canBlockWithAnotherWolf() {
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new HowlpackWolf());
        addCreatureReady(player1, new YoungWolf());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
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

        declareAttackersAndPrepareBlockers(player2, List.of(0));
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

    @Test
    @DisplayName("An opponent's Wolf does not allow blocking")
    void cannotBlockWithOnlyOpponentsWolf() {
        addCreatureReady(player2, new HowlpackWolf());
        addCreatureReady(player1, new HowlpackWolf());

        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An unrelated creature does not allow blocking")
    void cannotBlockWithOnlyAnotherBear() {
        addCreatureReady(player2, new HowlpackWolf());
        addCreatureReady(player1, new HowlpackWolf());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Wolf still allows blocking")
    void canBlockWithTappedSummoningSickWolf() {
        addCreatureReady(player2, new HowlpackWolf());
        addCreatureReady(player1, new HowlpackWolf());
        Permanent otherWolf = addCreatureReady(player1, new HowlpackWolf());
        otherWolf.tap();
        otherWolf.setSummoningSick(true);

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The other Wolf must still be controlled when blockers are declared")
    void cannotBlockAfterOtherWolfLeaves() {
        addCreatureReady(player2, new HowlpackWolf());
        addCreatureReady(player1, new HowlpackWolf());
        Permanent otherWolf = addCreatureReady(player1, new HowlpackWolf());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(otherWolf);
        gd.playerGraveyards.get(player1.getId()).add(otherWolf.getCard());

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }
}
