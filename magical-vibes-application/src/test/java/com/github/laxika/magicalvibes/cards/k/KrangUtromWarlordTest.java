package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KrangUtromWarlord.class, Juggernaut.class, FountainOfYouth.class, GrizzlyBears.class})
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
}
