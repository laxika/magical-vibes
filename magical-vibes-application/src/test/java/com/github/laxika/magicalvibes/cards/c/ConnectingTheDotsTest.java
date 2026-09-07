package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConnectingTheDots.class, GrizzlyBears.class})
class ConnectingTheDotsTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles one face-down card for each creature that attacks")
    void exilesTopCardForEachAttackingCreature() {
        Permanent source = castConnectingTheDots();
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(firstAttacker),
                gd.playerBattlefields.get(player1.getId()).indexOf(secondAttacker)));
        resolveAllTriggers();

        List<ExiledCardEntry> exiled = gd.exiledCards.stream()
                .filter(entry -> source.getId().equals(entry.sourcePermanentId()))
                .toList();
        assertThat(exiled).hasSize(2).allMatch(ExiledCardEntry::faceDown);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Discarding and sacrificing returns all exiled cards to their owners' hands")
    void activationReturnsExiledCardsToOwnersHands() {
        Permanent source = castConnectingTheDots();
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(firstAttacker),
                gd.playerBattlefields.get(player1.getId()).indexOf(secondAttacker)));
        resolveAllTriggers();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(source), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(first, second);
        assertThat(gd.getCardsExiledByPermanent(source.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Connecting the Dots");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private Permanent castConnectingTheDots() {
        harness.setHand(player1, List.of(new ConnectingTheDots()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Connecting the Dots");
    }
}
