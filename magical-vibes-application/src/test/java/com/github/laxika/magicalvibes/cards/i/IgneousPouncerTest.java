package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IgneousPouncer.class, Swamp.class, Mountain.class, Forest.class, GrizzlyBears.class})
class IgneousPouncerTest extends BaseCardTest {

    @Test
    @DisplayName("Swampcycling discards the card and offers only Swamp cards")
    void swampcyclingDiscardsAndOffersSwamps() {
        harness.setHand(player1, List.of(new IgneousPouncer()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        setupLibrary();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Igneous Pouncer");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Swamp"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Choosing a Swamp from the search puts it into hand")
    void choosingSwampPutsItIntoHand() {
        harness.setHand(player1, List.of(new IgneousPouncer()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        setupLibrary();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Swamp");
    }

    @Test
    @DisplayName("Mountaincycling discards the card and offers only Mountain cards")
    void mountaincyclingDiscardsAndOffersMountains() {
        harness.setHand(player1, List.of(new IgneousPouncer()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        setupLibrary();

        harness.ensurePriority(player1);
        harness.getGameService().activateHandAbility(gd, player1, 0, 1, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Igneous Pouncer");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Mountain"))
                .hasSize(1);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Swamp(), new Swamp(), new Mountain(),
                new Forest(), new GrizzlyBears()));
    }

    @Test
    @DisplayName("Mountaincycling puts the chosen Mountain into hand without drawing another card")
    void choosingMountainPutsOnlyItIntoHand() {
        harness.setHand(player1, List.of(new IgneousPouncer()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        setupLibrary();

        harness.ensurePriority(player1);
        gs.activateHandAbility(gd, player1, 0, 1, null);
        harness.assertInGraveyard(player1, "Igneous Pouncer");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Mountain");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4)
                .noneMatch(card -> card.getName().equals("Mountain"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Swampcycling may fail to find even when a Swamp is available")
    void mayDeclineToFindSwamp() {
        harness.setHand(player1, List.of(new IgneousPouncer()));
        harness.addMana(player1, ManaColor.RED, 2);
        setupLibrary();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Igneous Pouncer");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Swampcycling resolves without finding a card when no Swamps are in the library")
    void noMatchingSwampDoesNotDraw() {
        harness.setHand(player1, List.of(new IgneousPouncer()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLibrary(player1, List.of(new Mountain(), new Forest()));

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Igneous Pouncer");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Neither cycling ability can be activated with only one mana")
    void insufficientManaDoesNotDiscard() {
        harness.setHand(player1, List.of(new IgneousPouncer()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.ensurePriority(player1);

        for (int abilityIndex = 0; abilityIndex < 2; abilityIndex++) {
            int index = abilityIndex;
            assertThatThrownBy(() -> gs.activateHandAbility(gd, player1, 0, index, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Not enough mana");
        }

        harness.assertInHand(player1, "Igneous Pouncer");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Igneous Pouncer can attack the turn it is cast")
    void hasteAllowsImmediateAttack() {
        harness.setHand(player1, List.of(new IgneousPouncer()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isAttacking()).isTrue();
    }
}
