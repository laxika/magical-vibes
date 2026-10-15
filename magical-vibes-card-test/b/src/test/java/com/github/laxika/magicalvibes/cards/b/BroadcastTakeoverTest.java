package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.f.FugitiveDroid;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BroadcastTakeover.class, DarksteelIngot.class, FugitiveDroid.class, Island.class})
class BroadcastTakeoverTest extends BaseCardTest {

    @Test
    @DisplayName("Steals opponent artifacts, untaps them and gives them haste")
    void stealsUntapsAndHastesOpponentArtifacts() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new DarksteelIngot());
        ownArtifact.tap();
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        opponentArtifact.tap();

        castBroadcastTakeover();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownArtifact, opponentArtifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentArtifact);
        assertThat(ownArtifact.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, ownArtifact, Keyword.HASTE)).isFalse();
        assertThat(opponentArtifact.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentArtifact, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Returns the stolen artifacts and removes haste at end of turn")
    void returnsStolenArtifactsAtEndOfTurn() {
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());

        castBroadcastTakeover();

        harness.assertOnBattlefield(player1, "Darksteel Ingot");
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentArtifact);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(opponentArtifact);
        assertThat(gqs.hasKeyword(gd, opponentArtifact, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Steals every opposing artifact including artifact creatures, but leaves lands alone")
    void stealsMultipleArtifactsButNotNonArtifacts() {
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player2, new FugitiveDroid());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        firstArtifact.tap();
        secondArtifact.tap();
        artifactCreature.tap();
        land.tap();

        castBroadcastTakeover();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(firstArtifact, secondArtifact, artifactCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(land);
        for (Permanent artifact : new Permanent[]{firstArtifact, secondArtifact, artifactCreature}) {
            assertThat(artifact.isTapped()).isFalse();
            assertThat(gqs.hasKeyword(gd, artifact, Keyword.HASTE)).isTrue();
        }
        assertThat(land.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Resolves without opposing artifacts and does not affect your own artifacts")
    void resolvesWithoutOpposingArtifacts() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new DarksteelIngot());
        ownArtifact.tap();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());

        castBroadcastTakeover();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Broadcast Takeover");
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(ownArtifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(land);
        assertThat(ownArtifact.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, ownArtifact, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Artifacts entering after resolution are not stolen or granted haste")
    void doesNotAffectArtifactsEnteringLater() {
        castBroadcastTakeover();

        Permanent laterArtifact = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(laterArtifact);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(laterArtifact);
        assertThat(gqs.hasKeyword(gd, laterArtifact, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Control and haste persist during the end step")
    void effectsPersistThroughEndStep() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());

        castBroadcastTakeover();
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.HASTE)).isTrue();
    }

    private void castBroadcastTakeover() {
        harness.castFromHand(player1, new BroadcastTakeover(), "{2}{R}{R}{R}");
        harness.passBothPriorities();
    }
}
