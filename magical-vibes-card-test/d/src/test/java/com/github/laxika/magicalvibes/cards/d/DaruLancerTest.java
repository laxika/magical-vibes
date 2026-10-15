package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DaruLancer.class, GlorySeeker.class})
class DaruLancerTest extends BaseCardTest {

    @Test
    void firstStrikeLetsLancerSurviveMultipleBlockers() {
        Permanent lancer = addCreatureReady(player1, new DaruLancer());
        Permanent firstBlocker = addCreatureReady(player2, new GlorySeeker());
        Permanent secondBlocker = addCreatureReady(player2, new GlorySeeker());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveCombat(player1);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstBlocker.getId(), 2,
                secondBlocker.getId(), 1));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(lancer);
        assertThat(countPermanents(player2, "Glory Seeker")).isEqualTo(1);
        assertThat(lancer.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        Permanent lancer = castFaceDownLancer();
        assertThat(lancer.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        int lancerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(lancer);
        harness.turnFaceUp(player1, lancerIndex);
        harness.passBothPriorities();

        assertThat(lancer.isFaceDown()).isFalse();
    }

    @Test
    void turningFaceUpRequiresBothWhiteMana() {
        Permanent lancer = castFaceDownLancer();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        int lancerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(lancer);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, lancerIndex))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(lancer.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.turnFaceUp(player1, lancerIndex);

        assertThat(lancer.isFaceDown()).isFalse();
    }

    @Test
    void faceDownLancerTradesWithGlorySeekerWithoutFirstStrike() {
        Permanent lancer = castFaceDownLancer();
        lancer.setSummoningSick(false);
        Permanent blocker = addCreatureReady(player2, new GlorySeeker());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(lancer);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    void turningFaceUpBeforeCombatRestoresFirstStrike() {
        Permanent lancer = castFaceDownLancer();
        lancer.setSummoningSick(false);
        Permanent blocker = addCreatureReady(player2, new GlorySeeker());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lancer));

        assertThat(lancer.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(lancer);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(lancer.getMarkedDamage()).isZero();
    }

    private Permanent castFaceDownLancer() {
        harness.setHand(player1, List.of(new DaruLancer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        return findPermanent(player1, "Daru Lancer");
    }
}
