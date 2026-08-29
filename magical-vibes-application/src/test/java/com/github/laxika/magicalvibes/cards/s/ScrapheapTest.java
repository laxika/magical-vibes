package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.f.FountainOfRenewal;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScrapheapTest extends BaseCardTest {

    @Test
    void gainsLifeForArtifactsAndEnchantmentsPutIntoYourGraveyard() {
        harness.addToBattlefield(player1, new Scrapheap());
        harness.addToBattlefield(player1, new AuraOfSilence());
        var artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfRenewal());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.sacrificePermanent(player1, 1, artifact.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        harness.assertInGraveyard(player1, "Aura of Silence");
        harness.assertInGraveyard(player1, "Fountain of Renewal");
    }

    @Test
    void ignoresPermanentsPutIntoAnOpponentsGraveyard() {
        harness.addToBattlefield(player1, new Scrapheap());
        harness.addToBattlefield(player2, new AuraOfSilence());
        var artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfRenewal());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.sacrificePermanent(player2, 0, artifact.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }
}
