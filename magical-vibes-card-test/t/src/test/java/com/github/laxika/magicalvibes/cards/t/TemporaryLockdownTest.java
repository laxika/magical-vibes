package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BrokenWings;
import com.github.laxika.magicalvibes.cards.e.ElasIlKorSadisticPilgrim;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.cards.y.YavimayaIconoclast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        TemporaryLockdown.class,
        BrokenWings.class,
        ElasIlKorSadisticPilgrim.class,
        Forest.class,
        FountainOfYouth.class,
        GrizzlyBears.class,
        HillGiant.class,
        Naturalize.class,
        PropheticPrism.class,
        YavimayaIconoclast.class
})
class TemporaryLockdownTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles all nonland permanents with mana value 2 or less")
    void exilesMatchingPermanents() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new PropheticPrism());
        harness.addToBattlefield(player2, new HillGiant());

        castAndResolveLockdown();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Temporary Lockdown");
        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Prophetic Prism");

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Fountain of Youth", "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Prophetic Prism");
    }

    @Test
    @DisplayName("Exiled permanents return when Temporary Lockdown leaves")
    void exiledPermanentsReturnWhenSourceLeaves() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new FountainOfYouth());

        castAndResolveLockdown();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID lockdownId = harness.getPermanentId(player1, "Temporary Lockdown");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, lockdownId);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Fountain of Youth");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Nothing is exiled if Lockdown leaves before its entry trigger resolves")
    void sourceLeavesBeforeEntryTriggerResolves() {
        harness.addToBattlefield(player1, new YavimayaIconoclast());
        harness.addToBattlefield(player2, new YavimayaIconoclast());
        harness.setHand(player1, List.of(new TemporaryLockdown()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new BrokenWings()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Temporary Lockdown"));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Temporary Lockdown");
        harness.assertOnBattlefield(player1, "Yavimaya Iconoclast");
        harness.assertOnBattlefield(player2, "Yavimaya Iconoclast");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Returning creatures see each other enter simultaneously")
    void returningCreaturesEnterSimultaneously() {
        harness.addToBattlefield(player1, new YavimayaIconoclast());
        harness.addToBattlefield(player1, new ElasIlKorSadisticPilgrim());
        castAndResolveLockdown();
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new BrokenWings()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Temporary Lockdown"));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Yavimaya Iconoclast");
        harness.assertOnBattlefield(player1, "Elas il-Kor, Sadistic Pilgrim");
        harness.assertLife(player1, 21);
    }

    private void castAndResolveLockdown() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new TemporaryLockdown()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
    }
}
