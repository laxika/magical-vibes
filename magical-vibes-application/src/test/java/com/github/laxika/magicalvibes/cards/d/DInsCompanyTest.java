package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(DInsCompany.class)
class DInsCompanyTest extends BaseCardTest {

    @Test
    void hasLifelinkOnlyWhileControllingAnotherDwarf() {
        Permanent company = harness.addToBattlefieldAndReturn(player1, new DInsCompany());
        assertThat(gqs.hasKeyword(gd, company, Keyword.LIFELINK)).isFalse();

        Permanent dwarf = addCreatureReady(player1, creature("Dwarf", CardSubtype.DWARF));
        assertThat(gqs.hasKeyword(gd, company, Keyword.LIFELINK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(dwarf);
        assertThat(gqs.hasKeyword(gd, company, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void entersAndOffersADwarfOrEquipmentFromTheTopFour() {
        Card dwarf = creature("Top Dwarf", CardSubtype.DWARF);
        Card equipment = artifact("Top Equipment", CardSubtype.EQUIPMENT);
        Card bear = creature("Top Bear", CardSubtype.BEAR);
        Card other = creature("Top Other", CardSubtype.HUMAN);
        setLibrary(dwarf, equipment, bear, other);

        castDinsCompany();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(dwarf.getId(), equipment.getId());

        harness.handleMultipleCardsChosen(player1, List.of(equipment.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(equipment);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(dwarf, bear, other);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(dwarf, bear, other);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void decliningTheLibraryPickPutsAllFourCardsOnTheBottom() {
        Card dwarf = creature("Top Dwarf", CardSubtype.DWARF);
        Card equipment = artifact("Top Equipment", CardSubtype.EQUIPMENT);
        Card bear = creature("Top Bear", CardSubtype.BEAR);
        Card other = creature("Top Other", CardSubtype.HUMAN);
        setLibrary(dwarf, equipment, bear, other);

        castDinsCompany();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(dwarf, equipment, bear, other);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castDinsCompany() {
        harness.setHand(player1, List.of(new DInsCompany()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }

    private static Card creature(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(subtype));
        card.setPower(1);
        card.setToughness(1);
        return card;
    }

    private static Card artifact(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setSubtypes(List.of(subtype));
        return card;
    }
}
