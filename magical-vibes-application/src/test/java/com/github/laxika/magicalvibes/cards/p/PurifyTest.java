package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.n.NoMercy;
import com.github.laxika.magicalvibes.cards.t.ThranLens;
import com.github.laxika.magicalvibes.cards.t.TickingGnomes;
import com.github.laxika.magicalvibes.cards.y.YavimayaWurm;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({Purify.class, NoMercy.class, ThranLens.class, TickingGnomes.class, YavimayaWurm.class})
class PurifyTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys both artifacts and enchantments")
    void destroysArtifactsAndEnchantments() {
        harness.addToBattlefield(player1, new ThranLens());
        harness.addToBattlefield(player2, new NoMercy());
        harness.addToBattlefield(player2, new TickingGnomes());

        harness.castFromHand(player1, new Purify(), "{3}{W}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Thran Lens");
        harness.assertNotOnBattlefield(player2, "No Mercy");
        harness.assertNotOnBattlefield(player2, "Ticking Gnomes");
        harness.assertInGraveyard(player1, "Thran Lens");
        harness.assertInGraveyard(player2, "No Mercy");
        harness.assertInGraveyard(player2, "Ticking Gnomes");
    }

    @Test
    @DisplayName("Does not destroy creatures")
    void doesNotDestroyCreatures() {
        harness.addToBattlefield(player1, new YavimayaWurm());

        harness.castFromHand(player1, new Purify(), "{3}{W}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Yavimaya Wurm");
    }

    @Test
    @DisplayName("Purify goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.castFromHand(player1, new Purify(), "{3}{W}{W}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Purify");
    }
}
