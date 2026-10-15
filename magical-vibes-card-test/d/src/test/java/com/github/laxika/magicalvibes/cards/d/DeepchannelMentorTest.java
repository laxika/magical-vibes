package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.s.SafeholdSentry;
import com.github.laxika.magicalvibes.cards.s.SilkbindFaerie;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeepchannelMentor.class, DrownerInitiate.class, SafeholdSentry.class,
        SilkbindFaerie.class, Humble.class})
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

    @Test
    @DisplayName("A multicolored blue creature can't be blocked even by a flying creature")
    void multicoloredBlueCreatureCantBeBlocked() {
        harness.addToBattlefield(player1, new DeepchannelMentor());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new SilkbindFaerie());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SilkbindFaerie());

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("A blue creature losing all abilities remains unblockable while Mentor is present")
    void losingAbilitiesDoesNotRemoveMentorsBlockingRestriction() {
        harness.addToBattlefield(player1, new DeepchannelMentor());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new DrownerInitiate());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SafeholdSentry());
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, attacker.getId());

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("Mentor losing its ability allows other blue creatures to be blocked")
    void losingMentorsAbilityRemovesBlockingRestriction() {
        Permanent mentor = harness.addToBattlefieldAndReturn(player1, new DeepchannelMentor());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new DrownerInitiate());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SafeholdSentry());
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, mentor.getId());

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }
}
