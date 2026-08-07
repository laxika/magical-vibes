package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HarrowTest extends BaseCardTest {

    @Test
    @DisplayName("Casting sacrifices a land as an additional cost")
    void castSacrificesLand() {
        Permanent land = castHarrow();

        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, land.getCard().getName());
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Resolving offers only basic lands, destination untapped battlefield")
    void offersOnlyBasicLands() {
        castHarrow();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Both chosen basic lands enter the battlefield untapped")
    void bothChosenLandsEnterUntapped() {
        castHarrow();
        harness.passBothPriorities();

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().hasType(CardType.LAND))
                .noneMatch(Permanent::isTapped);
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Forest") || c.getName().equals("Plains"));
        harness.assertInGraveyard(player1, "Harrow");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Player may fail to find")
    void canFailToFind() {
        castHarrow();
        harness.passBothPriorities();

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot cast without a land to sacrifice")
    void cannotCastWithoutLand() {
        harness.setHand(player1, List.of(new Harrow()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot sacrifice a non-land permanent")
    void cannotSacrificeNonLand() {
        Permanent creature = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player1.getId()).add(creature);

        harness.setHand(player1, List.of(new Harrow()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent castHarrow() {
        Permanent land = new Permanent(new com.github.laxika.magicalvibes.cards.m.Mountain());
        gd.playerBattlefields.get(player1.getId()).add(land);

        harness.setHand(player1, List.of(new Harrow()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstantWithSacrifice(player1, 0, null, land.getId());

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Forest(), new GrizzlyBears()));
        return land;
    }
}
