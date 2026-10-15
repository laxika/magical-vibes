package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrafdiggersCage;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.m.MyrBattlesphere;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TransmuteArtifact.class, Millstone.class, Ornithopter.class,
        Triskelion.class, GrafdiggersCage.class, MyrBattlesphere.class})
class TransmuteArtifactTest extends BaseCardTest {

    @Test
    void putsArtifactWithLowerManaValueOntoBattlefield() {
        harness.addToBattlefield(player1, new Millstone());
        Permanent sacrificed = findPermanent(player1, "Millstone");
        harness.setLibrary(player1, List.of(new Ornithopter()));
        castTransmuteArtifact();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Millstone");
        harness.assertOnBattlefield(player1, "Ornithopter");
    }

    @Test
    void paysDifferenceToPutMoreExpensiveArtifactOntoBattlefield() {
        harness.addToBattlefield(player1, new Ornithopter());
        Permanent sacrificed = findPermanent(player1, "Ornithopter");
        harness.setLibrary(player1, List.of(new Millstone()));
        castTransmuteArtifact();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertOnBattlefield(player1, "Millstone");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void declinesDifferencePaymentAndPutsMoreExpensiveArtifactIntoOwnersGraveyard() {
        harness.addToBattlefield(player1, new Ornithopter());
        Permanent sacrificed = findPermanent(player1, "Ornithopter");
        harness.setLibrary(player1, List.of(new Millstone()));
        castTransmuteArtifact();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Millstone");
        harness.assertNotOnBattlefield(player1, "Millstone");
    }

    @Test
    void equalManaValueNeedsNoPayment() {
        harness.addToBattlefield(player1, new Millstone());
        Permanent sacrificed = findPermanent(player1, "Millstone");
        harness.setLibrary(player1, List.of(new Millstone()));
        castTransmuteArtifact();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Millstone");
        harness.assertOnBattlefield(player1, "Millstone");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void noArtifactToSacrificeDoesNotSearch() {
        Millstone libraryCard = new Millstone();
        harness.setLibrary(player1, List.of(libraryCard));
        castTransmuteArtifact();

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        harness.assertNotOnBattlefield(player1, "Millstone");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mayFailToFindAfterSacrificing() {
        harness.addToBattlefield(player1, new Ornithopter());
        Permanent sacrificed = findPermanent(player1, "Ornithopter");
        Millstone libraryCard = new Millstone();
        harness.setLibrary(player1, List.of(libraryCard));
        castTransmuteArtifact();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Ornithopter");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        harness.assertNotOnBattlefield(player1, "Millstone");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emptyLibraryStillSacrificesArtifact() {
        harness.addToBattlefield(player1, new Ornithopter());
        Permanent sacrificed = findPermanent(player1, "Ornithopter");
        harness.setLibrary(player1, List.of());
        castTransmuteArtifact();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, sacrificed.getId());

        harness.assertInGraveyard(player1, "Ornithopter");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void selectedArtifactStaysInLibraryUntilPaymentIsDecided() {
        harness.addToBattlefield(player1, new Ornithopter());
        Permanent sacrificed = findPermanent(player1, "Ornithopter");
        Millstone libraryCard = new Millstone();
        harness.setLibrary(player1, List.of(libraryCard));
        castTransmuteArtifact();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.assertNotInHand(player1, "Millstone");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void cageDoesNotPreventFindingCreatureAndDecliningPayment() {
        harness.addToBattlefield(player2, new GrafdiggersCage());
        harness.addToBattlefield(player1, new Ornithopter());
        Permanent sacrificed = findPermanent(player1, "Ornithopter");
        harness.setLibrary(player1, List.of(new Triskelion()));
        castTransmuteArtifact();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, sacrificed.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Triskelion");
        harness.assertNotOnBattlefield(player1, "Triskelion");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void paidArtifactCreatureTriggersItsEntersAbility() {
        harness.addToBattlefield(player1, new Ornithopter());
        Permanent sacrificed = findPermanent(player1, "Ornithopter");
        harness.setLibrary(player1, List.of(new MyrBattlesphere()));
        castTransmuteArtifact();
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Myr Battlesphere");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(4);
    }

    private void castTransmuteArtifact() {
        harness.castFromHand(player1, new TransmuteArtifact(), "{U}{U}");
    }

}
