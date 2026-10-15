package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FlaxenIntruder;
import com.github.laxika.magicalvibes.cards.g.GarenbrigPaladin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThunderousSnapper.class, GrizzlyBears.class, HillGiant.class, GarenbrigPaladin.class, FlaxenIntruder.class})
class ThunderousSnapperTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when its controller casts a spell with mana value exactly 5")
    void drawsForSpellWithManaValueFive() {
        harness.addToBattlefield(player1, new ThunderousSnapper());
        Card drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(highManaValueSorcery(5)));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Does not draw for a spell with mana value less than 5")
    void doesNotDrawForSpellWithManaValueFour() {
        harness.addToBattlefield(player1, new ThunderousSnapper());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not draw when an opponent casts a spell with mana value 5 or greater")
    void doesNotDrawForOpponentsSpell() {
        harness.addToBattlefield(player1, new ThunderousSnapper());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(highManaValueSorcery(5)));
        harness.addMana(player2, ManaColor.COLORLESS, 5);

        harness.castSorcery(player2, 0, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each Snapper draws independently for a qualifying creature spell")
    void eachSnapperDrawsForQualifyingCreature() {
        harness.addToBattlefield(player1, new ThunderousSnapper());
        harness.addToBattlefield(player1, new ThunderousSnapper());
        Card firstDraw = new ThunderousSnapper();
        Card secondDraw = new ThunderousSnapper();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new GarenbrigPaladin()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(countPermanents(player1, "Garenbrig Paladin")).isEqualTo(1);
    }

    @Test
    @DisplayName("A Snapper being cast does not trigger its own battlefield ability")
    void castingSnapperDoesNotDraw() {
        harness.setLibrary(player1, List.of(new ThunderousSnapper()));
        harness.setHand(player1, List.of(new ThunderousSnapper()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(countPermanents(player1, "Thunderous Snapper")).isEqualTo(1);
    }

    @Test
    @DisplayName("Uses the adventure spell's mana value rather than the creature's")
    void drawsForHighManaValueAdventure() {
        harness.addToBattlefield(player1, new ThunderousSnapper());
        Card drawnCard = new ThunderousSnapper();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new FlaxenIntruder()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAdventure(player1, 0, List.of());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    private Card highManaValueSorcery(int manaValue) {
        Card card = new Card();
        card.setName("High Mana Value Sorcery");
        card.setType(CardType.SORCERY);
        card.setManaCost("{" + manaValue + "}");
        card.setColor(CardColor.GREEN);
        card.setColors(List.of(CardColor.GREEN));
        return card;
    }
}
