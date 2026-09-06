package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RabbleRousing.class, GrizzlyBears.class})
class RabbleRousingTest extends BaseCardTest {

    @Test
    @DisplayName("Hideaway exiles one of the top five cards face down and bottoms the rest")
    void hideawayExilesOneCard() {
        List<Card> library = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears());
        Card chosen = library.get(2);
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new RabbleRousing()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(2));

        Permanent rabbleRousing = findPermanent(player1, "Rabble Rousing");
        ExiledCardEntry exiled = gd.findExiledCard(chosen.getId());
        assertThat(exiled).isNotNull();
        assertThat(exiled.faceDown()).isTrue();
        assertThat(gd.getImprintedCard(rabbleRousing.getCard())).isSameAs(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(chosen);
    }

    @Test
    @DisplayName("Attacking with multiple creatures creates that many Citizens")
    void createsCitizenTokensForAttackers() {
        Permanent rabbleRousing = addRabbleRousingWithImprint(new GrizzlyBears());
        addReadyCreature(new GrizzlyBears());
        addReadyCreature(new GrizzlyBears());
        addReadyCreature(new GrizzlyBears());

        declareAttackers(List.of(1, 2, 3));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Citizen")).hasSize(3);
        assertThat(findPermanents(player1, "Citizen")).allMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.getImprintedCard(rabbleRousing.getCard())).isNotNull();
    }

    @Test
    @DisplayName("After creating Citizens, ten creatures allow playing the imprinted card for free")
    void playsImprintedCardAtTenCreatures() {
        addRabbleRousingWithImprint(new GrizzlyBears());
        for (int i = 0; i < 9; i++) {
            addReadyCreature(new GrizzlyBears());
        }

        declareAttackers(List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Citizen")).hasSize(1);
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(10);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Declining the free play leaves the imprinted card in exile")
    void decliningFreePlayLeavesCardExiled() {
        Card imprinted = new GrizzlyBears();
        addRabbleRousingWithImprint(imprinted);
        for (int i = 0; i < 9; i++) {
            addReadyCreature(new GrizzlyBears());
        }

        declareAttackers(List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Citizen")).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(imprinted);
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(9);
    }

    private Permanent addRabbleRousingWithImprint(Card imprinted) {
        harness.addToBattlefield(player1, new RabbleRousing());
        GameData gameData = harness.getGameData();
        Permanent rabbleRousing = findPermanent(player1, "Rabble Rousing");
        gameData.setImprintedCard(rabbleRousing.getCard(), imprinted);
        gameData.addToExile(player1.getId(), imprinted);
        return rabbleRousing;
    }

    private Permanent addReadyCreature(Card creature) {
        return addCreatureReady(player1, creature);
    }
}
