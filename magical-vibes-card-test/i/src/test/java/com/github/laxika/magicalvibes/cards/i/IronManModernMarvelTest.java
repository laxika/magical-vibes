package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IronManModernMarvel.class, Memnite.class, GrizzlyBears.class, Spellbook.class})
class IronManModernMarvelTest extends BaseCardTest {

    @Test
    void boostsOtherArtifactCreaturesOnly() {
        Permanent ironMan = addCreatureReady(player1, new IronManModernMarvel());
        Permanent memnite = addCreatureReady(player1, new Memnite());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ironMan)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ironMan)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, memnite)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, memnite)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void attackingWithAnotherArtifactCreatureDraws() {
        addCreatureReady(player1, new IronManModernMarvel());
        addCreatureReady(player1, new Memnite());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    void doesNotDrawWithoutAnotherArtifactCreature() {
        addCreatureReady(player1, new IronManModernMarvel());
        addCreatureReady(player1, new Spellbook());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    void doesNotBoostOpponentsArtifactCreatures() {
        addCreatureReady(player1, new IronManModernMarvel());
        Permanent memnite = addCreatureReady(player2, new Memnite());

        assertThat(gqs.getEffectivePower(gd, memnite)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, memnite)).isEqualTo(1);
    }

    @Test
    void boostEndsWhenIronManLeavesBattlefield() {
        Permanent ironMan = addCreatureReady(player1, new IronManModernMarvel());
        Permanent memnite = addCreatureReady(player1, new Memnite());
        assertThat(gqs.getEffectivePower(gd, memnite)).isEqualTo(2);

        harness.getPermanentRemovalService().removePermanentToHand(gd, ironMan);

        assertThat(gqs.getEffectivePower(gd, memnite)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, memnite)).isEqualTo(1);
    }

    @Test
    void nonartifactCreatureAndOpponentsArtifactCreatureDoNotEnableDraw() {
        addCreatureReady(player1, new IronManModernMarvel());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new Memnite());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        assertThat(gd.stack).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    void doesNotDrawIfLastOtherArtifactCreatureLeavesBeforeResolution() {
        addCreatureReady(player1, new IronManModernMarvel());
        Permanent memnite = addCreatureReady(player1, new Memnite());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, memnite);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    void drawsIfAnotherArtifactCreatureRemainsAfterIronManLeaves() {
        Permanent ironMan = addCreatureReady(player1, new IronManModernMarvel());
        addCreatureReady(player1, new Memnite());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, ironMan);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    void anotherArtifactCreatureAttackingWithoutIronManDoesNotDraw() {
        addCreatureReady(player1, new IronManModernMarvel());
        addCreatureReady(player1, new Memnite());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(1));
        assertThat(gd.stack).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }
}
