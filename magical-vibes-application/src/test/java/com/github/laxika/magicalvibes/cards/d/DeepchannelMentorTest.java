package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.SafeholdSentry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeepchannelMentor.class, DrownerInitiate.class, SafeholdSentry.class})
class DeepchannelMentorTest extends BaseCardTest {

    @Test
    @DisplayName("Blue creature you control can't be blocked")
    void blueCreatureCantBeBlocked() {
        harness.addToBattlefield(player1, new DeepchannelMentor());
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new DrownerInitiate());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SafeholdSentry());

        assertThat(bls.canBlockAttacker(gd, blocker, wizard,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("Deepchannel Mentor itself can't be blocked")
    void mentorItselfCantBeBlocked() {
        Permanent mentor = harness.addToBattlefieldAndReturn(player1, new DeepchannelMentor());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SafeholdSentry());

        assertThat(bls.canBlockAttacker(gd, blocker, mentor,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("Non-blue creature you control can still be blocked")
    void nonBlueCreatureCanBeBlocked() {
        harness.addToBattlefield(player1, new DeepchannelMentor());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new SafeholdSentry());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SafeholdSentry());

        assertThat(bls.canBlockAttacker(gd, blocker, bears,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("Does not affect opponent's blue creature")
    void doesNotAffectOpponentBlueCreatures() {
        harness.addToBattlefield(player1, new DeepchannelMentor());
        Permanent opponentWizard = harness.addToBattlefieldAndReturn(player2, new DrownerInitiate());
        Permanent blocker = harness.addToBattlefieldAndReturn(player1, new SafeholdSentry());

        assertThat(bls.canBlockAttacker(gd, blocker, opponentWizard,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
    }

    @Test
    @DisplayName("Effect removed when Deepchannel Mentor leaves the battlefield")
    void effectRemovedWhenMentorLeaves() {
        Permanent mentor = harness.addToBattlefieldAndReturn(player1, new DeepchannelMentor());
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new DrownerInitiate());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SafeholdSentry());

        assertThat(bls.canBlockAttacker(gd, blocker, wizard,
                gd.playerBattlefields.get(player2.getId()))).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(mentor);

        assertThat(bls.canBlockAttacker(gd, blocker, wizard,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }
}
