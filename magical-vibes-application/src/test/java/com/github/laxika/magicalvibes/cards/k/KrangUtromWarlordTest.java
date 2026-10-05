package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KrangUtromWarlord.class, Juggernaut.class, FountainOfYouth.class, GrizzlyBears.class,
        Boomerang.class})
class KrangUtromWarlordTest extends BaseCardTest {

    @Test
    void grantsKeywordsToOtherArtifactCreaturesYouControl() {
        harness.addToBattlefield(player1, new KrangUtromWarlord());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new Juggernaut());

        assertThat(gqs.hasKeyword(gd, artifactCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, artifactCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, artifactCreature, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, artifactCreature, Keyword.HASTE)).isTrue();
    }

    @Test
    void doesNotGrantKeywordsToNonArtifactOrOpposingCreatures() {
        harness.addToBattlefield(player1, new KrangUtromWarlord());
        Permanent nonArtifactCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent opposingArtifactCreature = harness.addToBattlefieldAndReturn(player2, new Juggernaut());

        assertThat(gqs.hasKeyword(gd, nonArtifactCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingArtifactCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    void resolvingKrangGrantsAllKeywordsToExistingArtifactCreatures() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new Juggernaut());
        harness.castFromHand(player1, new KrangUtromWarlord(), "{9}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Krang, Utrom Warlord");
        for (Keyword keyword : List.of(Keyword.FLYING, Keyword.TRAMPLE, Keyword.INDESTRUCTIBLE, Keyword.HASTE)) {
            assertThat(gqs.hasKeyword(gd, artifactCreature, keyword)).isTrue();
        }
    }

    @Test
    void artifactCreatureEnteringLaterGainsKeywordsAndLosesThemWhenKrangLeaves() {
        Permanent krang = harness.addToBattlefieldAndReturn(player1, new KrangUtromWarlord());
        harness.castFromHand(player1, new Juggernaut(), "{4}");
        harness.passBothPriorities();
        Permanent artifactCreature = findPermanent(player1, "Juggernaut");

        for (Keyword keyword : List.of(Keyword.FLYING, Keyword.TRAMPLE, Keyword.INDESTRUCTIBLE, Keyword.HASTE)) {
            assertThat(gqs.hasKeyword(gd, artifactCreature, keyword)).isTrue();
        }

        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, krang.getId());

        harness.assertNotOnBattlefield(player1, "Krang, Utrom Warlord");
        harness.assertInHand(player1, "Krang, Utrom Warlord");
        harness.assertOnBattlefield(player1, "Juggernaut");
        for (Keyword keyword : List.of(Keyword.FLYING, Keyword.TRAMPLE, Keyword.INDESTRUCTIBLE, Keyword.HASTE)) {
            assertThat(gqs.hasKeyword(gd, artifactCreature, keyword)).isFalse();
        }
    }
}
