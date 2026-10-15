package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Flatten;
import com.github.laxika.magicalvibes.cards.n.Negate;
import com.github.laxika.magicalvibes.cards.s.ScionOfUgin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonlordsPrerogative.class, ScionOfUgin.class, Negate.class, Flatten.class})
class DragonlordsPrerogativeTest extends BaseCardTest {

    @Test
    @DisplayName("Draws four cards")
    void drawsFourCards() {
        DragonlordsPrerogative prerogative = new DragonlordsPrerogative();
        List<Card> drawnCards = List.of(new Negate(), new Negate(), new Negate(), new Negate());
        harness.setHand(player1, List.of(prerogative));
        harness.setLibrary(player1, drawnCards);
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrderElementsOf(drawnCards);
    }

    @Test
    @DisplayName("Cannot be countered when a Dragon is revealed from hand")
    void cannotBeCounteredWhenDragonIsRevealed() {
        DragonlordsPrerogative prerogative = new DragonlordsPrerogative();
        ScionOfUgin dragon = new ScionOfUgin();
        List<Card> drawnCards = List.of(new Negate(), new Negate(), new Negate(), new Negate());
        harness.setHand(player1, List.of(prerogative, dragon));
        harness.setLibrary(player1, drawnCards);
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.setHand(player2, List.of(new Negate()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstantWithDiscard(player1, 0, null, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, prerogative.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(dragon);
        assertThat(gd.playerHands.get(player1.getId())).containsAll(drawnCards);
        harness.assertInGraveyard(player2, "Negate");
    }

    @Test
    @DisplayName("Cannot be countered when a Dragon is controlled as cast")
    void cannotBeCounteredWhenDragonIsControlledAsCast() {
        addCreatureReady(player1, new ScionOfUgin());
        DragonlordsPrerogative prerogative = new DragonlordsPrerogative();
        List<Card> drawnCards = List.of(new Negate(), new Negate(), new Negate(), new Negate());
        harness.setHand(player1, List.of(prerogative));
        harness.setLibrary(player1, drawnCards);
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.setHand(player2, List.of(new Negate()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, prerogative.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsAll(drawnCards);
        harness.assertInGraveyard(player2, "Negate");
    }

    @Test
    @DisplayName("Can be countered without a revealed or controlled Dragon")
    void canBeCounteredWithoutDragon() {
        DragonlordsPrerogative prerogative = new DragonlordsPrerogative();
        List<Card> drawnCards = List.of(new Negate(), new Negate(), new Negate(), new Negate());
        harness.setHand(player1, List.of(prerogative, new Negate()));
        harness.setLibrary(player1, drawnCards);
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.setHand(player2, List.of(new Negate()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, prerogative.getId());

        assertThat(gd.playerHands.get(player1.getId())).doesNotContainAnyElementsOf(drawnCards);
        harness.assertInGraveyard(player1, "Dragonlord's Prerogative");
    }

    @Test
    @DisplayName("A Dragon in hand does not prevent countering when revealing is declined")
    void canBeCounteredWhenDragonRevealIsDeclined() {
        DragonlordsPrerogative prerogative = new DragonlordsPrerogative();
        ScionOfUgin dragon = new ScionOfUgin();
        harness.setHand(player1, List.of(prerogative, dragon));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.setHand(player2, List.of(new Negate()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, prerogative.getId());

        harness.assertInGraveyard(player1, "Dragonlord's Prerogative");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(dragon);
    }

    @Test
    @DisplayName("An opponent's Dragon does not prevent countering")
    void opponentsDragonDoesNotPreventCountering() {
        harness.addToBattlefield(player2, new ScionOfUgin());
        DragonlordsPrerogative prerogative = new DragonlordsPrerogative();
        harness.setHand(player1, List.of(prerogative));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.setHand(player2, List.of(new Negate()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, prerogative.getId());

        harness.assertInGraveyard(player1, "Dragonlord's Prerogative");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Remains uncounterable after the Dragon controlled as cast dies")
    void remainsUncounterableAfterDragonDies() {
        var dragon = harness.addToBattlefieldAndReturn(player1, new ScionOfUgin());
        DragonlordsPrerogative prerogative = new DragonlordsPrerogative();
        List<Card> drawnCards = List.of(new Negate(), new Negate(), new Negate(), new Negate());
        harness.setHand(player1, List.of(prerogative));
        harness.setLibrary(player1, drawnCards);
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.setHand(player2, List.of(new Flatten(), new Negate()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, dragon.getId());
        harness.assertInGraveyard(player1, "Scion of Ugin");
        harness.castAndResolveInstant(player2, 0, prerogative.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrderElementsOf(drawnCards);
        harness.assertInGraveyard(player1, "Dragonlord's Prerogative");
    }
}
