package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Crawlspace;
import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.cards.g.GrimMonolith;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Tinker.class, Crawlspace.class, GiantCockroach.class, GrimMonolith.class})
class TinkerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting sacrifices an artifact as an additional cost")
    void castSacrificesArtifact() {
        Permanent artifact = castTinker();

        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, artifact.getCard().getName());
        harness.assertInGraveyard(player1, "Grim Monolith");
    }

    @Test
    @DisplayName("Resolving offers only artifacts for the battlefield")
    void offersOnlyArtifacts() {
        castTinker();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .allMatch(c -> c.hasType(CardType.ARTIFACT))
                .noneMatch(c -> c.getName().equals("Giant Cockroach"));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("The chosen artifact enters the battlefield")
    void chosenArtifactEntersBattlefield() {
        castTinker();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Crawlspace"));
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Crawlspace"));
        harness.assertInGraveyard(player1, "Tinker");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can fail to find an artifact and still finish resolving")
    void canFailToFindArtifact() {
        castTinker();
        harness.setLibrary(player1, List.of(new GiantCockroach()));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Tinker");
        harness.assertNotOnBattlefield(player1, "Crawlspace");
    }

    @Test
    @DisplayName("Cannot cast without an artifact to sacrifice")
    void cannotCastWithoutArtifact() {
        harness.setHand(player1, List.of(new Tinker()));
        addTinkerMana();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot sacrifice a nonartifact permanent")
    void cannotSacrificeNonartifactPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());

        harness.setHand(player1, List.of(new Tinker()));
        addTinkerMana();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot sacrifice an artifact controlled by the opponent")
    void cannotSacrificeOpponentsArtifact() {
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new GrimMonolith());

        harness.setHand(player1, List.of(new Tinker()));
        addTinkerMana();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, opponentArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent castTinker() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new GrimMonolith());

        harness.setHand(player1, List.of(new Tinker()));
        addTinkerMana();
        harness.castSorceryWithSacrifice(player1, 0, artifact.getId());

        harness.setLibrary(player1, List.of(new Crawlspace(), new GiantCockroach()));
        return artifact;
    }

    private void addTinkerMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
