package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.Crusade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GhostQuarter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.l.LiquimetalCoating;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaveOfVitriol.class, Crusade.class, FountainOfYouth.class, Forest.class,
        GhostQuarter.class, GrizzlyBears.class, Plains.class, LiquimetalCoating.class})
class WaveOfVitriolTest extends BaseCardTest {

    private void cast() {
        harness.castFromHand(player1, new WaveOfVitriol(), "{5}{G}{G}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Sacrifices matching permanents and searches for one tapped basic land per nonbasic land")
    void sacrificesAndSearchesPerPlayer() {
        Permanent ownBasic = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player1, new Crusade());
        harness.addToBattlefield(player1, new GhostQuarter());
        harness.addToBattlefield(player1, new GhostQuarter());

        Permanent opponentBasic = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new GhostQuarter());

        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Plains()));

        cast();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownBasic, ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentBasic, opponentCreature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Ghost Quarter"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Ghost Quarter"));
        harness.assertInGraveyard(player1, "Fountain of Youth");
        harness.assertInGraveyard(player1, "Crusade");
        harness.assertInGraveyard(player2, "Fountain of Youth");

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().playerId()).isEqualTo(player1.getId());
        assertThat(search.params().remainingCount()).isEqualTo(2);
        assertThat(search.params().destination().name()).isEqualTo("BATTLEFIELD_TAPPED");

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().playerId()).isEqualTo(player2.getId());
        assertThat(search.params().remainingCount()).isEqualTo(1);

        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(CardType.LAND)))
                .hasSize(3)
                .filteredOn(Permanent::isTapped)
                .hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(CardType.LAND)))
                .hasSize(2)
                .filteredOn(Permanent::isTapped)
                .hasSize(1);
    }

    @Test
    @DisplayName("Does not search when no nonbasic land was sacrificed")
    void noSearchWithoutNonbasicLand() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player1, new Crusade());
        harness.setLibrary(player1, List.of(new Forest()));

        cast();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Fountain of Youth"))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Crusade"));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A basic land sacrificed as an artifact also grants a replacement land search")
    void searchesForSacrificedBasicArtifactLand() {
        harness.addToBattlefield(player1, new LiquimetalCoating());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(new Plains()));

        cast();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Liquimetal Coating");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement()
                .satisfies(permanent -> {
                    assertThat(permanent.getCard().getName()).isEqualTo("Plains");
                    assertThat(permanent.isTapped()).isTrue();
                });
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the optional search does not shuffle the library")
    void decliningSearchDoesNotShuffle() {
        harness.addToBattlefield(player1, new GhostQuarter());
        harness.setLibrary(player1, List.of(new Forest(), new Plains()));

        cast();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.gameLog).extracting(GameLogEntry::plainText)
                .noneMatch(text -> text.contains("library is shuffled"));
    }

    @Test
    @DisplayName("A player can find fewer basic lands than the number of lands sacrificed")
    void searchesWithTooFewBasicLands() {
        harness.addToBattlefield(player1, new GhostQuarter());
        harness.addToBattlefield(player1, new GhostQuarter());
        harness.setLibrary(player1, List.of(new Forest(), new GhostQuarter()));

        cast();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement()
                .satisfies(permanent -> {
                    assertThat(permanent.getCard().getName()).isEqualTo("Forest");
                    assertThat(permanent.isTapped()).isTrue();
                });
        assertThat(gd.playerDecks.get(player1.getId()))
                .singleElement()
                .satisfies(card -> assertThat(card.getName()).isEqualTo("Ghost Quarter"));
        assertThat(gd.gameLog).extracting(GameLogEntry::plainText)
                .filteredOn(text -> text.toLowerCase().contains("library is shuffled"))
                .hasSize(1);
    }
}
