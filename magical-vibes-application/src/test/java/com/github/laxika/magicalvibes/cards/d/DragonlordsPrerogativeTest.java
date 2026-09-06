package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RapaciousDragon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonlordsPrerogative.class, RapaciousDragon.class, GrizzlyBears.class, Island.class, Cancel.class})
class DragonlordsPrerogativeTest extends BaseCardTest {

    @Test
    @DisplayName("Draws four cards")
    void drawsFourCards() {
        DragonlordsPrerogative prerogative = new DragonlordsPrerogative();
        List<Card> drawnCards = List.of(new Island(), new Island(), new Island(), new Island());
        harness.setHand(player1, List.of(prerogative));
        harness.setLibrary(player1, drawnCards);
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrderElementsOf(drawnCards);
    }

    @Test
    @DisplayName("Cannot be countered when a Dragon is revealed from hand")
    void cannotBeCounteredWhenDragonIsRevealed() {
        DragonlordsPrerogative prerogative = new DragonlordsPrerogative();
        RapaciousDragon dragon = new RapaciousDragon();
        List<Card> drawnCards = List.of(new Island(), new Island(), new Island(), new Island());
        harness.setHand(player1, List.of(prerogative, dragon));
        harness.setLibrary(player1, drawnCards);
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, prerogative.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(dragon);
        assertThat(gd.playerHands.get(player1.getId())).containsAll(drawnCards);
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Cannot be countered when a Dragon is controlled as cast")
    void cannotBeCounteredWhenDragonIsControlledAsCast() {
        addCreatureReady(player1, new RapaciousDragon());
        DragonlordsPrerogative prerogative = new DragonlordsPrerogative();
        List<Card> drawnCards = List.of(new Island(), new Island(), new Island(), new Island());
        harness.setHand(player1, List.of(prerogative));
        harness.setLibrary(player1, drawnCards);
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, prerogative.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsAll(drawnCards);
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Can be countered without a revealed or controlled Dragon")
    void canBeCounteredWithoutDragon() {
        DragonlordsPrerogative prerogative = new DragonlordsPrerogative();
        List<Card> drawnCards = List.of(new Island(), new Island(), new Island(), new Island());
        harness.setHand(player1, List.of(prerogative, new GrizzlyBears()));
        harness.setLibrary(player1, drawnCards);
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, prerogative.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContainAnyElementsOf(drawnCards);
        harness.assertInGraveyard(player1, "Dragonlord's Prerogative");
    }
}
