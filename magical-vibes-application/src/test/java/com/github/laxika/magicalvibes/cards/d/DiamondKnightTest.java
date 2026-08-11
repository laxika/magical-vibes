package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiamondKnightTest extends BaseCardTest {

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
    @DisplayName("Choosing a color as Diamond Knight enters stores that color")
    void choosesColorOnEntry() {
        harness.setHand(player1, List.of(new DiamondKnight()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.ColorChoice.class))
                .isNotNull();
        harness.handleListChoice(player1, "GREEN");

        assertThat(findPermanent(player1, "Diamond Knight").getChosenColor()).isEqualTo(CardColor.GREEN);
    }

    @Test
    @DisplayName("Casting a spell containing the chosen color puts a +1/+1 counter on Diamond Knight")
    void putsCounterForChosenColorSpell() {
        harness.addToBattlefield(player1, new DiamondKnight());
        Permanent knight = findPermanent(player1, "Diamond Knight");
        knight.setChosenColor(CardColor.GREEN);

        harness.setHand(player1, List.of(createCreature("Green Creature", List.of(CardColor.GREEN))));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A multicolored spell containing the chosen color puts a counter on Diamond Knight")
    void putsCounterForMulticoloredChosenColorSpell() {
        harness.addToBattlefield(player1, new DiamondKnight());
        Permanent knight = findPermanent(player1, "Diamond Knight");
        knight.setChosenColor(CardColor.GREEN);

        harness.setHand(player1, List.of(createCreature("Green White Creature",
                List.of(CardColor.GREEN, CardColor.WHITE))));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A spell without the chosen color does not put a counter on Diamond Knight")
    void doesNotPutCounterForOtherColorSpell() {
        harness.addToBattlefield(player1, new DiamondKnight());
        Permanent knight = findPermanent(player1, "Diamond Knight");
        knight.setChosenColor(CardColor.GREEN);

        harness.setHand(player1, List.of(createCreature("Red Creature", List.of(CardColor.RED))));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
