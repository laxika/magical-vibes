package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainportBell;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThistledownPlayers.class, FountainportBell.class, Plains.class})
class ThistledownPlayersTest extends BaseCardTest {

    @Test
    void attackUntapsTargetNonlandPermanent() {
        addCreatureReady(player1, new ThistledownPlayers());
        Permanent target = addCreatureReady(player2, new ThistledownPlayers());
        target.tap();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void attackTriggerCannotTargetLand() {
        addCreatureReady(player1, new ThistledownPlayers());
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canUntapItselfAndRemainAttacking() {
        Permanent players = addCreatureReady(player1, new ThistledownPlayers());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            assertThat(players.isTapped()).isTrue();
            harness.handlePermanentChosen(player1, players.getId());
            harness.passBothPriorities();

            assertThat(players.isTapped()).isFalse();
            assertThat(players.isAttacking()).isTrue();
        });
    }

    @Test
    void attackUntapsOwnNoncreatureArtifact() {
        addCreatureReady(player1, new ThistledownPlayers());
        Permanent bell = harness.addToBattlefieldAndReturn(player1, new FountainportBell());
        bell.tap();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bell.getId());
        harness.passBothPriorities();

        assertThat(bell.isTapped()).isFalse();
    }

    @Test
    void canChooseAnUntappedPermanentWithoutUntappingOtherPermanents() {
        Permanent players = addCreatureReady(player1, new ThistledownPlayers());
        Permanent bell = harness.addToBattlefieldAndReturn(player2, new FountainportBell());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bell.getId());
        harness.passBothPriorities();

        assertThat(bell.isTapped()).isFalse();
        assertThat(players.isTapped()).isTrue();
    }

    @Test
    void removedTargetDoesNotUntapAnotherPermanent() {
        Permanent players = addCreatureReady(player1, new ThistledownPlayers());
        Permanent bell = harness.addToBattlefieldAndReturn(player2, new FountainportBell());
        bell.tap();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bell.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bell));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bell);
        assertThat(players.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
