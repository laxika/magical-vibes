package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EvercoatUrsine.class, GrizzlyBears.class, Plains.class})
class EvercoatUrsineTest extends BaseCardTest {

    @Test
    @DisplayName("Hideaway 3 twice exiles two face-down cards with Evercoat Ursine")
    void hideawayExilesTwoCardsWithSource() {
        EvercoatUrsine ursine = new EvercoatUrsine();
        List<Card> library = List.of(
                new GrizzlyBears(), new Plains(), new GrizzlyBears(),
                new Plains(), new GrizzlyBears(), new Plains());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(ursine));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        chooseExileCard(0);
        chooseExileCard(0);

        Permanent permanent = findPermanent(player1, "Evercoat Ursine");
        assertThat(gd.getCardsExiledByPermanent(permanent.getId())).hasSize(2);
        assertThat(gd.getCardsExiledByPermanent(permanent.getId()))
                .allMatch(card -> {
                    ExiledCardEntry entry = gd.findExiledCard(card.getId());
                    return entry != null && entry.faceDown()
                            && permanent.getId().equals(entry.sourcePermanentId());
                });
    }

    @Test
    @DisplayName("Combat damage offers a land exiled with Evercoat Ursine for free")
    void combatDamageOffersExiledLand() {
        Permanent ursine = addCreatureReady(player1, new EvercoatUrsine());
        Plains plains = new Plains();
        gd.addToExile(player1.getId(), plains, ursine.getId(), true);

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Plains");
        assertThat(gd.landsPlayedThisTurn.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        assertThat(gd.findExiledCard(plains.getId())).isNull();
    }

    @Test
    @DisplayName("Playing one exiled card withdraws the other offers")
    void onlyOneExiledCardCanBePlayed() {
        Permanent ursine = addCreatureReady(player1, new EvercoatUrsine());
        Plains plains = new Plains();
        GrizzlyBears bears = new GrizzlyBears();
        gd.addToExile(player1.getId(), plains, ursine.getId(), true);
        gd.addToExile(player1.getId(), bears, ursine.getId(), true);

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(bears);
        harness.assertOnBattlefield(player1, "Plains");
    }

    private void chooseExileCard(int index) {
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(index));
    }
}
