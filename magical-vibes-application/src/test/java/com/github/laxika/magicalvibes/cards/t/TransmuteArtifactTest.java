package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TransmuteArtifact.class, GoldMyr.class, Spellbook.class})
class TransmuteArtifactTest extends BaseCardTest {

    @Test
    void putsArtifactWithLowerManaValueOntoBattlefield() {
        harness.addToBattlefield(player1, new GoldMyr());
        Permanent sacrificed = findPermanent(player1, "Gold Myr");
        setLibrary(new Spellbook());
        castTransmuteArtifact();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Gold Myr");
        harness.assertOnBattlefield(player1, "Spellbook");
    }

    @Test
    void paysDifferenceToPutMoreExpensiveArtifactOntoBattlefield() {
        harness.addToBattlefield(player1, new Spellbook());
        Permanent sacrificed = findPermanent(player1, "Spellbook");
        setLibrary(new GoldMyr());
        castTransmuteArtifact();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertOnBattlefield(player1, "Gold Myr");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void declinesDifferencePaymentAndPutsMoreExpensiveArtifactIntoOwnersGraveyard() {
        harness.addToBattlefield(player1, new Spellbook());
        Permanent sacrificed = findPermanent(player1, "Spellbook");
        setLibrary(new GoldMyr());
        castTransmuteArtifact();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Gold Myr");
        harness.assertNotOnBattlefield(player1, "Gold Myr");
    }

    private void castTransmuteArtifact() {
        harness.castFromHand(player1, new TransmuteArtifact(), "{U}{U}");
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }
}
