package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HulkSmash.class, CrawWurm.class, GrizzlyBears.class, MindStone.class, Ornithopter.class})
class HulkSmashTest extends BaseCardTest {

    @Test
    void destroysTargetNoncreatureArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());

        cast(new int[]{0}, List.of(artifact.getId()), List.of());

        harness.assertNotOnBattlefield(player2, "Mind Stone");
        harness.assertInGraveyard(player2, "Mind Stone");
    }

    @Test
    void creatureYouControlDealsDamageEqualToItsPowerToOpponentCreature() {
        Permanent source = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new CrawWurm());

        cast(new int[]{1}, List.of(source.getId(), target.getId()), List.of());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void teamworkResolvesBothModesWithModeSpecificTargets() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        Permanent source = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new CrawWurm());
        Permanent teammate = addCreatureReady(player1, new CrawWurm());

        cast(new int[]{0, 1}, List.of(artifact.getId(), source.getId(), target.getId()),
                List.of(teammate.getId()));

        harness.assertNotOnBattlefield(player2, "Mind Stone");
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(teammate.isTapped()).isTrue();
    }

    @Test
    void teamworkCannotBePaidWhenOnlyOneModeIsChosen() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        Permanent teammate = addCreatureReady(player1, new CrawWurm());

        assertThatThrownBy(() -> cast(new int[]{0}, List.of(artifact.getId()), List.of(teammate.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("requires choosing all modes");
        assertThat(teammate.isTapped()).isFalse();
    }

    @Test
    void modesRejectInvalidTargets() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        assertThatThrownBy(() -> cast(new int[]{0}, List.of(artifactCreature.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, List<java.util.UUID> targetIds, List<java.util.UUID> teamworkIds) {
        harness.setHand(player1, List.of(new HulkSmash()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castModalSorceryWithModesAndTaps(
                player1, 0, 1, 2, modes, targetIds, teamworkIds);
        harness.passBothPriorities();
    }
}
