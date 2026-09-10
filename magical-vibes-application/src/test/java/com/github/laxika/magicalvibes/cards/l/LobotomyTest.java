package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.StalkingStones;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Lobotomy.class, Forest.class, StalkingStones.class, TrainedArmodon.class})
class LobotomyTest extends BaseCardTest {

    @Test
    @DisplayName("Only cards other than basic lands in the revealed hand are choosable")
    void basicLandsAreNotChoosable() {
        castLobotomyAt(List.of(new TrainedArmodon(), new Lobotomy(), new Forest(), new StalkingStones()));

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options())
                .containsExactlyInAnyOrder("Trained Armodon", "Lobotomy", "Stalking Stones");
    }

    @Test
    @DisplayName("Exiles every copy of the chosen card from the target's hand, graveyard, and library")
    void exilesAllCopiesFromAllZones() {
        harness.setGraveyard(player2, List.of(new TrainedArmodon()));
        castLobotomyAt(List.of(new TrainedArmodon(), new TrainedArmodon(), new Lobotomy()));
        harness.setLibrary(player2, List.of(new TrainedArmodon()));

        harness.handleListChoice(player1, "Trained Armodon");

        long exiled = gd.getPlayerExiledCards(player2.getId()).stream()
                .filter(card -> card.getName().equals("Trained Armodon")).count();
        assertThat(exiled).isEqualTo(4);
        harness.assertNotInHand(player2, "Trained Armodon");
        harness.assertNotInGraveyard(player2, "Trained Armodon");
        assertThat(gd.playerDecks.get(player2.getId())).noneMatch(card -> card.getName().equals("Trained Armodon"));

        harness.assertInHand(player2, "Lobotomy");
    }

    @Test
    @DisplayName("A nonbasic land can be chosen and exiled")
    void nonbasicLandCanBeChosen() {
        castLobotomyAt(List.of(new StalkingStones(), new TrainedArmodon()));

        harness.handleListChoice(player1, "Stalking Stones");

        harness.assertNotInHand(player2, "Stalking Stones");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Stalking Stones"));
    }

    @Test
    @DisplayName("Copies on the battlefield are not affected")
    void doesNotAffectBattlefieldCopies() {
        addCreatureReady(player2, new TrainedArmodon());
        castLobotomyAt(List.of(new TrainedArmodon()));

        harness.handleListChoice(player1, "Trained Armodon");

        harness.assertOnBattlefield(player2, "Trained Armodon");
    }

    @Test
    @DisplayName("No choice is made when the revealed hand holds only basic lands")
    void noPromptWhenHandIsAllBasicLands() {
        castLobotomyAt(List.of(new Forest()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("Still shuffles the target's library when no card can be chosen")
    void shufflesLibraryWhenNoCardCanBeChosen() {
        harness.setLibrary(player2, List.of(new TrainedArmodon(), new StalkingStones()));
        castLobotomyAt(List.of(new Forest()));

        assertThat(gameLogContains("shuffles their library")).isTrue();
    }

    @Test
    @DisplayName("No choice is made when the target's hand is empty")
    void noPromptWhenHandEmpty() {
        castLobotomyAt(List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The caster may target themselves")
    void mayTargetSelf() {
        harness.setHand(player1, List.of(new Lobotomy(), new TrainedArmodon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly("Trained Armodon");
    }

    private void castLobotomyAt(List<Card> targetHand) {
        harness.setHand(player2, targetHand);
        harness.setHand(player1, List.of(new Lobotomy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
