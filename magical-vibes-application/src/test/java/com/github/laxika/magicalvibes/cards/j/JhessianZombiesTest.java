package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.e.EthercasteKnight;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JhessianZombies.class, Island.class, Swamp.class, Forest.class, GrizzlyBears.class,
        EthercasteKnight.class})
class JhessianZombiesTest extends BaseCardTest {

    @Test
    @DisplayName("Islandcycling discards the card and offers only Island cards")
    void islandcyclingDiscardsAndOffersIslands() {
        harness.setHand(player1, List.of(new JhessianZombies()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        setupLibrary();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Jhessian Zombies");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Island"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Choosing an Island from the search puts it into hand")
    void choosingIslandPutsItIntoHand() {
        harness.setHand(player1, List.of(new JhessianZombies()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        setupLibrary();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Island");
    }


    @Test
    @DisplayName("Swampcycling discards the card and offers only Swamp cards")
    void swampcyclingDiscardsAndOffersSwamps() {
        harness.setHand(player1, List.of(new JhessianZombies()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        setupLibrary();

        harness.ensurePriority(player1);
        harness.getGameService().activateHandAbility(gd, player1, 0, 1, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Jhessian Zombies");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Swamp"))
                .hasSize(1);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Swamp(),
                new Forest(), new GrizzlyBears()));
    }
    @Test
    void choosingSwampPutsItIntoHandAndRevealsIt() {
        harness.setHand(player1, List.of(new JhessianZombies()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        setupLibrary();
        harness.ensurePriority(player1);
        gs.activateHandAbility(gd, player1, 0, 1, null);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Swamp");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4)
                .noneMatch(card -> card.getName().equals("Swamp"));
        assertThat(gameLogContains("reveals Swamp")).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void cyclingDiscardsAsCostBeforeTheSearchResolves(int abilityIndex) {
        harness.setHand(player1, List.of(new JhessianZombies()));
        harness.addMana(player1, ManaColor.RED, 2);
        setupLibrary();
        harness.ensurePriority(player1);

        gs.activateHandAbility(gd, player1, 0, abilityIndex, null);

        harness.assertInGraveyard(player1, "Jhessian Zombies");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void cyclingCannotBeActivatedWithOnlyOneMana(int abilityIndex) {
        harness.setHand(player1, List.of(new JhessianZombies()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> gs.activateHandAbility(gd, player1, 0, abilityIndex, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Jhessian Zombies");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void cyclingMayFailToFindEvenWithMatchingCards(int abilityIndex) {
        harness.setHand(player1, List.of(new JhessianZombies()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        setupLibrary();
        harness.ensurePriority(player1);
        gs.activateHandAbility(gd, player1, 0, abilityIndex, null);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Jhessian Zombies");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void cyclingResolvesWithoutMatchingCards(int abilityIndex) {
        harness.setHand(player1, List.of(new JhessianZombies()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.ensurePriority(player1);
        gs.activateHandAbility(gd, player1, 0, abilityIndex, null);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Jhessian Zombies");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void cyclingResolvesWithAnEmptyLibrary(int abilityIndex) {
        harness.setHand(player1, List.of(new JhessianZombies()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setLibrary(player1, List.of());
        harness.ensurePriority(player1);
        gs.activateHandAbility(gd, player1, 0, abilityIndex, null);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Jhessian Zombies");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void fearPreventsNonblackNonartifactBlockers() {
        addCreatureReady(player1, new JhessianZombies()).setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("(fear)");
    }

    @Test
    void fearAllowsBlackBlockers() {
        addCreatureReady(player1, new JhessianZombies()).setAttacking(true);
        addCreatureReady(player2, new JhessianZombies());
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    void fearAllowsNonblackArtifactBlockers() {
        addCreatureReady(player1, new JhessianZombies()).setAttacking(true);
        addCreatureReady(player2, new EthercasteKnight());
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }
}
