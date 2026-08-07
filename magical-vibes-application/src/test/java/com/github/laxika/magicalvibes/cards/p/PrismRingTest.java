package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PrismRingTest extends BaseCardTest {

    private static Card createCreature(String name, List<CardColor> colors) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{2}");
        card.setColors(colors);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    @Test
    @DisplayName("Choosing a color as the ring enters stores exactly that color")
    void choosesOneColorOnEntry() {
        harness.setHand(player1, List.of(new PrismRing()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.ColorChoice.class))
                .isNotNull();
        harness.handleListChoice(player1, "GREEN");

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.ColorChoice.class))
                .isNull();
        assertThat(findPermanent(player1, "Prism Ring").getChosenColors())
                .containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("Casting a spell of the chosen color gains one life")
    void gainsOneLifeForChosenColorSpell() {
        harness.addToBattlefield(player1, new PrismRing());
        Permanent ring = findPermanent(player1, "Prism Ring");
        ring.getChosenColors().add(CardColor.GREEN);

        harness.setHand(player1, List.of(createCreature("Green Creature", List.of(CardColor.GREEN))));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("A multicolored spell including the chosen color still gains only one life")
    void gainsOnlyOneLifeForMulticoloredSpell() {
        harness.addToBattlefield(player1, new PrismRing());
        Permanent ring = findPermanent(player1, "Prism Ring");
        ring.getChosenColors().add(CardColor.GREEN);

        harness.setHand(player1, List.of(createCreature("Green White Creature",
                List.of(CardColor.GREEN, CardColor.WHITE))));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("A spell of another color does not trigger the ring")
    void doesNotTriggerForOtherColor() {
        harness.addToBattlefield(player1, new PrismRing());
        Permanent ring = findPermanent(player1, "Prism Ring");
        ring.getChosenColors().add(CardColor.GREEN);

        harness.setHand(player1, List.of(createCreature("Red Creature", List.of(CardColor.RED))));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }
}
