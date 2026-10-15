package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VenomizedCat.class, Forest.class})
class VenomizedCatTest extends BaseCardTest {

    @Test
    void millsTwoCardsWhenItEnters() {
        Forest first = new Forest();
        Forest second = new Forest();
        VenomizedCat third = new VenomizedCat();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.castFromHand(player1, new VenomizedCat(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    void millsOnlyCardsAvailableInLibrary() {
        Forest card = new Forest();
        harness.setLibrary(player1, List.of(card));
        harness.castFromHand(player1, new VenomizedCat(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
    }

    @Test
    void emptyLibraryDoesNotCauseLossWhenMilling() {
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, new VenomizedCat(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        harness.assertOnBattlefield(player1, "Venomized Cat");
    }

    @Test
    void millsOnlyItsControllerLibraryWhenOpponentCastsIt() {
        Forest untouched = new Forest();
        Forest first = new Forest();
        VenomizedCat second = new VenomizedCat();
        Forest third = new Forest();
        harness.setLibrary(player1, List.of(untouched));
        harness.setLibrary(player2, List.of(first, second, third));
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new VenomizedCat(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void enterTriggerStillMillsAfterSourceLeavesBattlefield() {
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.castFromHand(player1, new VenomizedCat(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second, third);
        harness.getPermanentRemovalService().removePermanentToHand(gd,
                findPermanent(player1, "Venomized Cat"));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        harness.assertInHand(player1, "Venomized Cat");
        harness.assertNotOnBattlefield(player1, "Venomized Cat");
    }

    @Test
    void deathtouchDestroysCreaturesDespiteNonlethalCombatDamage() {
        addCreatureReady(player1, new VenomizedCat());
        harness.addToBattlefield(player2, new VenomizedCat());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Venomized Cat");
        harness.assertInGraveyard(player2, "Venomized Cat");
        harness.assertNotOnBattlefield(player1, "Venomized Cat");
        harness.assertNotOnBattlefield(player2, "Venomized Cat");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
