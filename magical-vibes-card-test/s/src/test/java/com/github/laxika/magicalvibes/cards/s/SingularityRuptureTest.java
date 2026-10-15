package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SingularityRupture.class, GrizzlyBears.class, Island.class})
class SingularityRuptureTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all creatures, then mills each targeted player half their library")
    void destroysCreaturesAndMillsEachTargetedPlayer() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, libraryCards(11));
        harness.setLibrary(player2, libraryCards(11));
        harness.setHand(player1, List.of(new SingularityRupture()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(player1.getId(), player2.getId()));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(6);
    }

    @Test
    @DisplayName("Allows no target players and still destroys all creatures")
    void allowsNoTargetPlayers() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, libraryCards(11));
        harness.setLibrary(player2, libraryCards(11));
        harness.setHand(player1, List.of(new SingularityRupture()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(11);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(11);
    }

    @Test
    @DisplayName("Can target only players")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SingularityRupture()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a player");
    }

    @Test
    @DisplayName("Mills only the selected player and leaves noncreature permanents intact")
    void millsOnlySelectedPlayerAndPreservesLands() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.setLibrary(player1, libraryCards(8));
        harness.setLibrary(player2, libraryCards(10));
        harness.setHand(player1, List.of(new SingularityRupture()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(8);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(5);
        harness.assertOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player2, "Island");
    }

    @Test
    @DisplayName("Can target only its controller")
    void millsOnlyControllerWhenSelected() {
        harness.setLibrary(player1, libraryCards(8));
        harness.setLibrary(player2, libraryCards(10));
        harness.setHand(player1, List.of(new SingularityRupture()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(player1.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(10);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Empty and one-card libraries mill nothing")
    void millsNothingFromTinyLibraries() {
        harness.setLibrary(player1, libraryCards(0));
        harness.setLibrary(player2, libraryCards(1));
        harness.setHand(player1, List.of(new SingularityRupture()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(player1.getId(), player2.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private List<Card> libraryCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }
}
