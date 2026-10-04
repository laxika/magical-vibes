package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DogWalker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AssembleThePlayers.class, GrizzlyBears.class, SerraAngel.class, DogWalker.class, Aluren.class})
class AssembleThePlayersTest extends BaseCardTest {

    @Test
    @DisplayName("Casts a creature with power 2 or less from the top of the library")
    void castsEligibleCreatureFromLibraryTop() {
        harness.addToBattlefield(player1, new AssembleThePlayers());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("Rejects a creature with power greater than 2")
    void rejectsCreatureWithPowerGreaterThanTwo() {
        harness.addToBattlefield(player1, new AssembleThePlayers());
        SerraAngel angel = new SerraAngel();
        harness.setLibrary(player1, List.of(angel));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(angel);
    }

    @Test
    @DisplayName("Allows only one matching creature cast from the top each turn")
    void allowsOnlyOneMatchingCreatureCastEachTurn() {
        harness.addToBattlefield(player1, new AssembleThePlayers());
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveFromLibraryTop(player1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    void onlyControllerCanLookAtIneligibleTopCardWithoutMana() {
        harness.addToBattlefield(player1, new AssembleThePlayers());
        harness.setLibrary(player1, List.of(new SerraAngel()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Serra Angel"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("Serra Angel"));
    }

    @Test
    void canStillLookAfterUsingCastingPermission() {
        harness.addToBattlefield(player1, new AssembleThePlayers());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new SerraAngel()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveFromLibraryTop(player1);
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Serra Angel"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("Serra Angel"));
    }

    @Test
    void rejectsNoncreatureSpell() {
        harness.addToBattlefield(player1, new AssembleThePlayers());
        AssembleThePlayers topCard = new AssembleThePlayers();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void doesNotGrantFlash() {
        harness.addToBattlefield(player1, new AssembleThePlayers());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }

    @Test
    void eachCopyGrantsAnIndependentCastingPermission() {
        harness.addToBattlefield(player1, new AssembleThePlayers());
        harness.addToBattlefield(player1, new AssembleThePlayers());
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        GrizzlyBears third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveFromLibraryTop(player1);
        harness.castAndResolveFromLibraryTop(player1);

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(2);
        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
    }

    @Test
    void castingPermissionResetsOnTheNextTurn() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new AssembleThePlayers());
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears drawn = new GrizzlyBears();
        GrizzlyBears next = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, drawn, next));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveFromLibraryTop(player1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveFromLibraryTop(player1);

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void alternativeManaCostStillUsesTheOncePerTurnPermission() {
        harness.addToBattlefield(player1, new AssembleThePlayers());
        harness.addToBattlefield(player1, new Aluren());
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    void offersDisguiseFromLibraryEvenWhenFaceUpPowerExceedsTwo() {
        harness.addToBattlefield(player1, new AssembleThePlayers());
        harness.setLibrary(player1, List.of(new DogWalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"playableLibraryTopCards\":[{")
                        && message.contains("Dog Walker"));

        gs.playCardFromLibraryTop(gd, player1, null, null, List.of(), List.of(), true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Dog Walker")
                        && permanent.isFaceDown());
    }
}
