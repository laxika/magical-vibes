package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TitanOfLittjara.class})
class TitanOfLittjaraTest extends BaseCardTest {

    @Test
    @DisplayName("ETB draws for each other creature sharing any type, then discards")
    void etbDrawsForEachOtherCreatureSharingAnyTypeThenDiscards() {
        harness.addToBattlefield(player1, creature("Bear", CardSubtype.BEAR));
        harness.addToBattlefield(player1, creature("Illusion", CardSubtype.ILLUSION));
        harness.addToBattlefield(player1, creature("Elf", CardSubtype.ELF));
        harness.setLibrary(player1, List.of(card("Draw one"), card("Draw two")));

        Card discard = card("Discard me");
        harness.setHand(player1, new ArrayList<>(List.of(new TitanOfLittjara(), discard)));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.BEAR.name());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Draw one", "Draw two");
        harness.assertInGraveyard(player1, "Discard me");
    }

    @Test
    @DisplayName("Attack trigger can be declined")
    void attackTriggerCanBeDeclined() {
        PermanentSetup setup = addTitanWithSharedCreatures(player1);
        Card discard = card("Keep me");
        harness.setHand(player1, new ArrayList<>(List.of(discard)));
        harness.setLibrary(player1, List.of(card("Should not draw")));

        declareAttackers(List.of(setup.titanIndex()));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Keep me");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private PermanentSetup addTitanWithSharedCreatures(Player player) {
        var titan = addCreatureReady(player, new TitanOfLittjara());
        titan.setChosenSubtype(CardSubtype.BEAR);
        harness.addToBattlefield(player, creature("Bear", CardSubtype.BEAR));
        harness.addToBattlefield(player, creature("Illusion", CardSubtype.ILLUSION));
        return new PermanentSetup(gd.playerBattlefields.get(player.getId()).indexOf(titan));
    }

    private Card creature(String name, CardSubtype subtype) {
        Card card = card(name);
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(subtype));
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private Card card(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        return card;
    }

    private record PermanentSetup(int titanIndex) {
    }
}
