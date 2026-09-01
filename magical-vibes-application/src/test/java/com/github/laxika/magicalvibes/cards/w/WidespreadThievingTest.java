package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.QasaliAmbusher;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WidespreadThieving.class, GrizzlyBears.class, QasaliAmbusher.class})
class WidespreadThievingTest extends BaseCardTest {

    @Test
    @DisplayName("Hideaway 5 exiles one card face down and puts the rest on the bottom")
    void hideawayExilesOneCardAndBottomsTheRest() {
        Card chosen = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        Card fourth = new GrizzlyBears();
        Card fifth = new GrizzlyBears();
        harness.setLibrary(player1, List.of(chosen, second, third, fourth, fifth));
        harness.setHand(player1, List.of(new WidespreadThieving()));
        addWidespreadMana();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent thieving = findPermanent(player1, "Widespread Thieving");
        ExiledCardEntry exiled = gd.findExiledCard(chosen.getId());
        assertThat(exiled).isNotNull();
        assertThat(exiled.faceDown()).isTrue();
        assertThat(gd.getImprintedCard(thieving.getCard())).isSameAs(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("A multicolored spell creates a Treasure and can pay to play the exiled card")
    void multicoloredSpellCreatesTreasureAndPaysToPlayExiledCard() {
        addThievingWithImprint(new GrizzlyBears());
        harness.setHand(player1, List.of(new QasaliAmbusher()));
        addQasaliAndWidespreadMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countTreasures()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Declining the payment keeps the imprinted card exiled")
    void decliningPaymentKeepsImprintedCardExiled() {
        Card imprinted = new GrizzlyBears();
        addThievingWithImprint(imprinted);
        harness.setHand(player1, List.of(new QasaliAmbusher()));
        addQasaliAndWidespreadMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countTreasures()).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(imprinted);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    private void addThievingWithImprint(Card imprinted) {
        harness.addToBattlefield(player1, new WidespreadThieving());
        Permanent thieving = findPermanent(player1, "Widespread Thieving");
        gd.setImprintedCard(thieving.getCard(), imprinted);
        gd.addToExile(player1.getId(), imprinted);
    }

    private void addWidespreadMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void addQasaliAndWidespreadMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private int countTreasures() {
        return (int) gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Treasure"))
                .count();
    }
}
