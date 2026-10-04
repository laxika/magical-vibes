package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GraspingLongneck;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HorridVigor.class, GrizzlyBears.class, Forest.class, GraspingLongneck.class})
class HorridVigorTest extends BaseCardTest {

    @Test
    void grantsDeathtouchAndIndestructibleToTargetCreature() {
        Permanent target = addCreature(player2);

        castOn(target);

        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void grantedKeywordsWearOffAtEndOfTurn() {
        Permanent target = addCreature(player1);

        castOn(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new HorridVigor()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotGrantKeywordsToOtherCreatures() {
        Permanent target = addCreature(player1);
        Permanent otherFriendly = addCreature(player1);
        Permanent opposing = addCreature(player2);

        castOn(target);

        for (Permanent other : List.of(otherFriendly, opposing)) {
            assertThat(gqs.hasKeyword(gd, other, Keyword.DEATHTOUCH)).isFalse();
            assertThat(gqs.hasKeyword(gd, other, Keyword.INDESTRUCTIBLE)).isFalse();
        }
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void doesNotResolveWhenTargetLeavesBattlefield() {
        Permanent target = addCreature(player2);
        Permanent remaining = addCreature(player2);
        harness.setHand(player1, List.of(new HorridVigor()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Horrid Vigor");
        assertThat(gqs.hasKeyword(gd, remaining, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, remaining, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @CardUsed({HorridVigor.class, GraspingLongneck.class})
    void protectedCreatureSurvivesLethalCombatDamageAndKillsLargerBlocker() {
        Permanent attacker = addCreatureReady(player1, new GraspingLongneck());
        Permanent blocker = addCreatureReady(player2, new GraspingLongneck());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        castOn(attacker);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertInGraveyard(player2, "Grasping Longneck");
        harness.assertLife(player2, 22);
    }

    private Permanent addCreature(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }

    private void castOn(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new HorridVigor()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
