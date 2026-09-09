package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EndOfTheHuntTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the opponent's creature with greatest mana value")
    void exilesGreatestManaValueCreature() {
        Permanent smaller = addCreature(player2, "Smaller creature", "{1}");
        Permanent greatest = addCreature(player2, "Greatest creature", "{3}{B}");

        castEndOfTheHunt();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .containsExactly(smaller.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Greatest creature");
    }

    @Test
    @DisplayName("Compares creatures and planeswalkers together")
    void exilesGreatestManaValuePlaneswalker() {
        Permanent creature = addCreature(player2, "Greatest creature", "{4}{B}");
        Permanent planeswalker = addPlaneswalker(player2, "Greatest planeswalker", "{4}{B}{B}", 5);

        castEndOfTheHunt();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .containsExactly(creature.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Greatest planeswalker");
    }

    @Test
    @DisplayName("The opponent chooses among tied greatest mana values")
    void choosesAmongTiedGreatestManaValues() {
        Permanent smaller = addCreature(player2, "Smaller creature", "{1}");
        Permanent tiedCreature = addCreature(player2, "Tied creature", "{3}{B}");
        Permanent tiedPlaneswalker = addPlaneswalker(player2, "Tied planeswalker", "{2}{B}{B}", 4);

        castEndOfTheHunt();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds())
                .containsExactlyInAnyOrder(tiedCreature.getId(), tiedPlaneswalker.getId());

        harness.handlePermanentChosen(player2, tiedPlaneswalker.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .containsExactly(smaller.getId(), tiedCreature.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Tied planeswalker");
    }

    @Test
    @DisplayName("Does nothing when the opponent controls no creature or planeswalker")
    void doesNothingWithoutEligiblePermanent() {
        addPermanent(player2, "Artifact", CardType.ARTIFACT, "{6}");

        castEndOfTheHunt();

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    private void castEndOfTheHunt() {
        harness.setHand(player1, List.of(new EndOfTheHunt()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private Permanent addCreature(Player player, String name, String manaCost) {
        return addPermanent(player, name, CardType.CREATURE, manaCost, card -> {
            card.setPower(1);
            card.setToughness(1);
        });
    }

    private Permanent addPlaneswalker(Player player, String name, String manaCost, int loyalty) {
        Permanent permanent = addPermanent(player, name, CardType.PLANESWALKER, manaCost,
                card -> card.setLoyalty(loyalty));
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        return permanent;
    }

    private Permanent addPermanent(Player player, String name, CardType type, String manaCost) {
        return addPermanent(player, name, type, manaCost, card -> { });
    }

    private Permanent addPermanent(Player player, String name, CardType type, String manaCost,
                                   java.util.function.Consumer<Card> configure) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setManaCost(manaCost);
        configure.accept(card);
        Permanent permanent = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
