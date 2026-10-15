package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.ChangelingOutcast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Throatseeker.class, GrizzlyBears.class, ChangelingOutcast.class})
class ThroatseekerTest extends BaseCardTest {

    @Test
    @DisplayName("Unblocked attacking Ninjas you control have lifelink")
    void unblockedAttackingNinjasHaveLifelink() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent firstNinja = addCreatureReady(player1, new Throatseeker());
        Permanent secondNinja = addCreatureReady(player1, new Throatseeker());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0, 1, 2));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of()));
        assertThat(gqs.hasKeyword(gd, firstNinja, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondNinja, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.LIFELINK)).isFalse();

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(26);
        assertThat(gd.getLife(player2.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Blocked Ninjas do not have lifelink")
    void blockedNinjasDoNotHaveLifelink() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new Throatseeker());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void nonattackingNinjasAndAttackersBeforeBlockersAreDeclaredDoNotHaveLifelink() {
        Permanent source = addCreatureReady(player1, new Throatseeker());
        Permanent ninja = addCreatureReady(player1, new ChangelingOutcast());

        assertThat(gqs.hasKeyword(gd, source, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, ninja, Keyword.LIFELINK)).isFalse();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(1)));
        assertThat(gqs.hasKeyword(gd, ninja, Keyword.LIFELINK)).isFalse();

        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of()));
        assertThat(gqs.hasKeyword(gd, source, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, ninja, Keyword.LIFELINK)).isTrue();

        harness.passBothPriorities();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gqs.hasKeyword(gd, ninja, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void opponentsUnblockedNinjasDoNotGainLifelink() {
        addCreatureReady(player1, new Throatseeker());
        Permanent ninja = addCreatureReady(player2, new ChangelingOutcast());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player1, List.of()));
        assertThat(gqs.hasKeyword(gd, ninja, Keyword.LIFELINK)).isFalse();

        harness.passBothPriorities();
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    void lifelinkIsLostWhenTheSourceLeavesBeforeCombatDamage() {
        Permanent source = addCreatureReady(player1, new Throatseeker());
        Permanent ninja = addCreatureReady(player1, new ChangelingOutcast());

        declareAttackersAndPrepareBlockers(List.of(1));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of()));
        assertThat(gqs.hasKeyword(gd, ninja, Keyword.LIFELINK)).isTrue();

        harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, source);
        assertThat(gqs.hasKeyword(gd, ninja, Keyword.LIFELINK)).isFalse();
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }
}
