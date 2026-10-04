package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Exterminatus.class, GrizzlyBears.class, DuskImp.class, Forest.class, MindStone.class, SolRing.class})
class ExterminatusTest extends BaseCardTest {

    @Test
    @DisplayName("Removes opponents' indestructible before destroying all nonland permanents")
    void removesOpponentsIndestructibleBeforeWipe() {
        Permanent ownIndestructible = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ownIndestructible.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        Permanent ownOrdinary = harness.addToBattlefieldAndReturn(player1, new DuskImp());
        Permanent opponentIndestructible = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentIndestructible.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        Permanent opponentOrdinary = harness.addToBattlefieldAndReturn(player2, new DuskImp());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.castFromHand(player1, new Exterminatus(), "{5}{W}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownIndestructible, ownLand)
                .doesNotContain(ownOrdinary);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentLand)
                .doesNotContain(opponentIndestructible, opponentOrdinary);
    }

    @Test
    @DisplayName("Destroys noncreature artifacts but preserves own indestructible artifacts and lands")
    void destroysArtifactsAndLeavesIndestructibleLandsAlone() {
        Permanent ownIndestructible = harness.addToBattlefieldAndReturn(player1, new SolRing());
        ownIndestructible.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.addToBattlefield(player1, new MindStone());
        Permanent opponentIndestructible = harness.addToBattlefieldAndReturn(player2, new SolRing());
        opponentIndestructible.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.addToBattlefield(player2, new MindStone());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        opponentLand.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        harness.castFromHand(player1, new Exterminatus(), "{5}{W}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sol Ring");
        harness.assertInGraveyard(player1, "Mind Stone");
        harness.assertInGraveyard(player2, "Sol Ring");
        harness.assertInGraveyard(player2, "Mind Stone");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gqs.hasKeyword(gd, ownIndestructible, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentLand, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's permanent can regenerate after losing indestructible")
    void permitsRegenerationAfterRemovingIndestructible() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SolRing());
        artifact.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        artifact.setRegenerationShield(1);

        harness.castFromHand(player1, new Exterminatus(), "{5}{W}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Sol Ring");
        harness.assertNotInGraveyard(player2, "Sol Ring");
        assertThat(artifact.isTapped()).isTrue();
        assertThat(artifact.getRegenerationShield()).isZero();
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.INDESTRUCTIBLE)).isFalse();

        Permanent laterArtifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        laterArtifact.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        assertThat(gqs.hasKeyword(gd, laterArtifact, Keyword.INDESTRUCTIBLE)).isTrue();
    }
}
