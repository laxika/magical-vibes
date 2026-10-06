package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CribSwap;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Realmwalker.class, GrizzlyBears.class, LlanowarElves.class})
class RealmwalkerTest extends BaseCardTest {

    @Test
    @DisplayName("casts a creature spell of the chosen type from the top of the library")
    void castsCreatureOfChosenTypeFromLibraryTop() {
        castRealmwalkerChoosingBear();

        Card bear = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bear));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(bear);
    }

    @Test
    @DisplayName("does not cast a creature of a different type from the top of the library")
    void rejectsCreatureOfDifferentTypeFromLibraryTop() {
        castRealmwalkerChoosingBear();
        Card elf = new LlanowarElves();
        harness.setLibrary(player1, List.of(elf));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(elf);
    }

    @Test
    void castsChangelingOfAnyChosenCreatureType() {
        castRealmwalkerChoosingBear();
        Realmwalker changeling = new Realmwalker();
        harness.setLibrary(player1, List.of(changeling));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveFromLibraryTop(player1);
        harness.handleListChoice(player1, "ELF");

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @CardUsed({CribSwap.class})
    void rejectsNoncreatureChangelingDespiteMatchingChosenType() {
        castRealmwalkerChoosingBear();
        Card spell = new CribSwap();
        harness.setLibrary(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1,
                harness.getPermanentId(player1, "Realmwalker")))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(spell);
    }

    @Test
    void showsTopCardOnlyToControllerEvenWhenItCannotBeCast() {
        castRealmwalkerChoosingBear();
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Llanowar Elves")
                        && message.contains("}],[]]"));
        assertThat(harness.getConn2().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[],[]]"));
    }

    @Test
    void stopsShowingAndAllowingTopCardWhenRealmwalkerLeaves() {
        castRealmwalkerChoosingBear();
        Card bear = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bear));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[],[]]"));
        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bear);
    }

    @Test
    void cannotCastMatchingCreatureOutsideMainPhase() {
        castRealmwalkerChoosingBear();
        Card bear = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bear));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bear);
    }

    @Test
    void mustPayNormalManaCostForMatchingCreature() {
        castRealmwalkerChoosingBear();
        Card bear = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bear));

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bear);
    }

    @Test
    void canCastMultipleMatchingCreaturesInOneTurn() {
        castRealmwalkerChoosingBear();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveFromLibraryTop(player1);
        harness.castAndResolveFromLibraryTop(player1);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    void cannotCastNextCreatureWhileFirstCreatureIsOnStack() {
        castRealmwalkerChoosingBear();
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromLibraryTop(player1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void realmwalkerWithoutChosenTypeAllowsLookingButNotCasting() {
        harness.addToBattlefield(player1, new Realmwalker());
        Card changeling = new Realmwalker();
        harness.setLibrary(player1, List.of(changeling));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Realmwalker")
                        && message.contains("}],[]]"));
        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(changeling);
    }

    private void castRealmwalkerChoosingBear() {
        Realmwalker realmwalker = new Realmwalker();
        harness.setHand(player1, List.of(realmwalker));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");
    }
}
