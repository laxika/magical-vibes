package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoaringSlagwurm.class, Ornithopter.class, GrizzlyBears.class, DarksteelIngot.class})
class RoaringSlagwurmTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking taps every artifact on every battlefield")
    void attackingTapsAllArtifacts() {
        Permanent slagwurm = addCreatureReady(player1, new RoaringSlagwurm());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent nonArtifact = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(slagwurm)));
        harness.passBothPriorities();

        assertThat(ownArtifact.isTapped()).isTrue();
        assertThat(opponentArtifact.isTapped()).isTrue();
        assertThat(nonArtifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Attack trigger taps noncreature artifacts, including indestructible artifacts")
    void attackingTapsNoncreatureArtifacts() {
        Permanent slagwurm = addCreatureReady(player1, new RoaringSlagwurm());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new DarksteelIngot());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        Permanent alreadyTapped = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        alreadyTapped.tap();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(slagwurm)));
        harness.passBothPriorities();

        assertThat(ownArtifact.isTapped()).isTrue();
        assertThat(opponentArtifact.isTapped()).isTrue();
        assertThat(alreadyTapped.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Artifacts are tapped on resolution even when the attacker has left the battlefield")
    void triggerUsesArtifactsPresentOnResolutionAndSurvivesSourceRemoval() {
        Permanent slagwurm = addCreatureReady(player1, new RoaringSlagwurm());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(slagwurm))));

        assertThat(gd.stack).hasSize(1);
        assertThat(artifact.isTapped()).isFalse();
        Permanent newArtifact = harness.addToBattlefieldAndReturn(player1, new DarksteelIngot());
        gd.playerBattlefields.get(player1.getId()).remove(slagwurm);
        gd.playerGraveyards.get(player1.getId()).add(slagwurm.getCard());
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
        assertThat(newArtifact.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Attack trigger resolves normally without any artifacts")
    void attackingWithoutArtifacts() {
        Permanent slagwurm = addCreatureReady(player1, new RoaringSlagwurm());
        Permanent nonAttackingSlagwurm = harness.addToBattlefieldAndReturn(player2, new RoaringSlagwurm());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(slagwurm)));
        harness.passBothPriorities();

        assertThat(nonAttackingSlagwurm.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
