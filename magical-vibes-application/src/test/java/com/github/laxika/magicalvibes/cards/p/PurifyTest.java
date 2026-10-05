package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GhituEncampment;
import com.github.laxika.magicalvibes.cards.n.NoMercy;
import com.github.laxika.magicalvibes.cards.t.ThranLens;
import com.github.laxika.magicalvibes.cards.t.TickingGnomes;
import com.github.laxika.magicalvibes.cards.y.YavimayaWurm;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({Purify.class, GhituEncampment.class, NoMercy.class, ThranLens.class, TickingGnomes.class, YavimayaWurm.class})
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

    @Test
    @DisplayName("Destroys the caster's enchantments and opposing artifacts")
    void destroysMatchingPermanentsRegardlessOfController() {
        harness.addToBattlefield(player1, new NoMercy());
        harness.addToBattlefield(player2, new ThranLens());

        harness.castFromHand(player1, new Purify(), "{3}{W}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "No Mercy");
        harness.assertNotOnBattlefield(player2, "Thran Lens");
        harness.assertInGraveyard(player1, "No Mercy");
        harness.assertInGraveyard(player2, "Thran Lens");
    }

    @Test
    @DisplayName("Leaves nonartifact, nonenchantment lands on both battlefields")
    void doesNotDestroyLands() {
        harness.addToBattlefield(player1, new GhituEncampment());
        harness.addToBattlefield(player2, new GhituEncampment());
        harness.addToBattlefield(player2, new ThranLens());

        harness.castFromHand(player1, new Purify(), "{3}{W}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ghitu Encampment");
        harness.assertOnBattlefield(player2, "Ghitu Encampment");
        harness.assertInGraveyard(player2, "Thran Lens");
    }

    @Test
    @DisplayName("Does not affect artifact or enchantment cards in hand or graveyard")
    void doesNotAffectCardsOutsideBattlefield() {
        harness.setHand(player2, List.of(new ThranLens(), new NoMercy()));
        harness.setGraveyard(player2, List.of(new TickingGnomes(), new NoMercy()));

        harness.castFromHand(player1, new Purify(), "{3}{W}{W}");
        harness.passBothPriorities();

        harness.assertInHand(player2, "Thran Lens");
        harness.assertInHand(player2, "No Mercy");
        harness.assertInGraveyard(player2, "Ticking Gnomes");
        harness.assertInGraveyard(player2, "No Mercy");
        harness.assertInGraveyard(player1, "Purify");
    }
}
