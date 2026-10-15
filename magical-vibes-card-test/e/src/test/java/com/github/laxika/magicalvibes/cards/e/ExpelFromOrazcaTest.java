package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.OrazcaRelic;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExpelFromOrazca.class, Forest.class, GrizzlyBears.class, Island.class, OrazcaRelic.class})
class ExpelFromOrazcaTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target nonland permanent to its owner's hand without the city's blessing")
    void returnsTargetToHandWithoutBlessing() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        castOn(targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Ascend grants the city's blessing before the top-of-library choice")
    void acceptsTopOfLibraryChoiceAfterAscending() {
        for (int i = 0; i < 10; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        castOn(targetId);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the city's blessing choice returns the permanent to its owner's hand")
    void declinesTopOfLibraryChoice() {
        gd.playersWithCityBlessing.add(player1.getId());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        castOn(targetId);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Island());
        UUID targetId = harness.getPermanentId(player2, "Island");
        harness.setHand(player1, List.of(new ExpelFromOrazca()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    @Test
    @DisplayName("Nine permanents do not grant the city's blessing")
    void doesNotAscendWithNinePermanents() {
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.addToBattlefield(player2, new GrizzlyBears());

        castOn(harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Ascend happens before bouncing your tenth permanent and the blessing persists")
    void keepsBlessingAfterBouncingTenthPermanent() {
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.addToBattlefield(player1, new GrizzlyBears());

        castOn(harness.getPermanentId(player1, "Grizzly Bears"));
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(9);
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
    }

    @Test
    @DisplayName("An illegal target prevents the spell from granting the city's blessing")
    void doesNotAscendWhenTargetLeavesBeforeResolution() {
        for (int i = 0; i < 10; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        var target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ExpelFromOrazca()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, target));

        harness.passBothPriorities();

        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
        harness.assertInGraveyard(player1, "Expel from Orazca");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Ascend counts permanents at resolution rather than at casting")
    void ascendsWhenTenthPermanentArrivesBeforeResolution() {
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new ExpelFromOrazca()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, targetId);
        harness.addToBattlefield(player1, new Forest());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can bounce a noncreature artifact without the city's blessing")
    void returnsNoncreatureArtifactToHand() {
        harness.addToBattlefield(player2, new OrazcaRelic());

        castOn(harness.getPermanentId(player2, "Orazca Relic"));

        harness.assertNotOnBattlefield(player2, "Orazca Relic");
        harness.assertInHand(player2, "Orazca Relic");
    }

    @Test
    @DisplayName("The opponent's blessing does not enable the library destination")
    void opponentBlessingDoesNotEnableLibraryChoice() {
        gd.playersWithCityBlessing.add(player2.getId());
        harness.addToBattlefield(player2, new GrizzlyBears());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        castOn(harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
    }

    @Test
    @DisplayName("A stolen permanent goes to its owner's library, not its controller's")
    void putsStolenPermanentOnOwnersLibrary() {
        gd.playersWithCityBlessing.add(player1.getId());
        var target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.stolenCreatures.put(target.getId(), player2.getId());
        int controllerDeckSize = gd.playerDecks.get(player1.getId()).size();
        int ownerDeckSize = gd.playerDecks.get(player2.getId()).size();

        castOn(target.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(controllerDeckSize);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(ownerDeckSize + 1);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    private void castOn(UUID targetId) {
        harness.setHand(player1, List.of(new ExpelFromOrazca()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
