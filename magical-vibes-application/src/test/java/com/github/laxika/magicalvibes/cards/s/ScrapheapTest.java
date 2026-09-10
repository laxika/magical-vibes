package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AuraFlux;
import com.github.laxika.magicalvibes.cards.e.EnchantedEvening;
import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.cards.t.ThranLens;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

@CardUsed({Scrapheap.class, AuraFlux.class, ThranLens.class, GiantCockroach.class})
class ScrapheapTest extends BaseCardTest {

    @Test
    void gainsLifeForArtifactsAndEnchantmentsPutIntoYourGraveyard() {
        harness.addToBattlefield(player1, new Scrapheap());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new AuraFlux());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ThranLens());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        removeToGraveyard(enchantment);
        removeToGraveyard(artifact);
        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore + 2);
        harness.assertInGraveyard(player1, "Aura Flux");
        harness.assertInGraveyard(player1, "Thran Lens");
    }

    @Test
    void ignoresNonArtifactAndEnchantmentPermanents() {
        harness.addToBattlefield(player1, new Scrapheap());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        removeToGraveyard(creature);
        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore);
    }

    @Test
    void ignoresPermanentsPutIntoAnOpponentsGraveyard() {
        harness.addToBattlefield(player1, new Scrapheap());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new AuraFlux());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ThranLens());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        removeToGraveyard(enchantment);
        removeToGraveyard(artifact);
        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @CardUsed(EnchantedEvening.class)
    void recognizesPermanentsThatAreMadeEnchantments() {
        harness.addToBattlefield(player1, new Scrapheap());
        harness.addToBattlefield(player1, new EnchantedEvening());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        removeToGraveyard(creature);
        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore + 1);
    }

    private void removeToGraveyard(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
    }
}
