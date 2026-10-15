package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DiamondKnight.class, GreenwoodSentinel.class})
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
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new DiamondKnight());
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
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new DiamondKnight());
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
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new DiamondKnight());
        knight.setChosenColor(CardColor.GREEN);

        harness.setHand(player1, List.of(createCreature("Red Creature", List.of(CardColor.RED))));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void counterTriggerResolvesBeforeChosenColorSpell() {
        harness.setHand(player1, List.of(new DiamondKnight(), new GreenwoodSentinel()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");
        Permanent knight = findPermanent(player1, "Diamond Knight");

        harness.castCreature(player1, 0);
        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Greenwood Sentinel");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Greenwood Sentinel");
        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void opponentsChosenColorSpellDoesNotTrigger() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new DiamondKnight());
        knight.setChosenColor(CardColor.GREEN);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GreenwoodSentinel()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Greenwood Sentinel");
        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void colorlessSpellDoesNotTriggerEvenWhenGreenManaIsSpent() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new DiamondKnight());
        knight.setChosenColor(CardColor.GREEN);
        harness.setHand(player1, List.of(new DiamondKnight()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
