package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.Frogify;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({GrolnokTheOmnivore.class, GrizzlyBears.class, Forest.class, Shock.class, Frogify.class})
class GrolnokTheOmnivoreTest extends BaseCardTest {
    @Test
    @DisplayName("A creature turned into a Frog triggers milling when it attacks")
    void transformedFrogAttackMills() {
        harness.addToBattlefield(player1, new GrolnokTheOmnivore());
        var bear = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Frogify()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, bear.getId());
        harness.passBothPriorities();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCardsWithCroakCounters).contains(forest.getId());
    }

    @Test
    @DisplayName("An old exile trigger cannot follow a card through a new graveyard entry")
    void exileTriggerDoesNotFollowNewGraveyardEntry() {
        addCreatureReady(player1, new GrolnokTheOmnivore());
        Card bear = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bear));
        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bear);

        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(bear));
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(bear));
        gd.markGraveyardEntry(bear);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bear);
        assertThat(gd.findExiledCard(bear.getId())).isNull();
    }

    @Test
    @DisplayName("Croak counters alone do not permit casting without Grolnok")
    void croakCountersRequireGrolnok() {
        Card bear = new GrizzlyBears();
        harness.setExile(player1, List.of(bear));
        gd.exiledCardsWithCroakCounters.add(bear.getId());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(bear.getId())).isNotNull();
    }

    @Test
    @DisplayName("A Frog attack mills three cards and exiles milled permanent cards with croak counters")
    void frogAttackMillsAndExilesPermanentCards() {
        addCreatureReady(player1, new GrolnokTheOmnivore());
        Card bear = new GrizzlyBears();
        Card forest = new Forest();
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(bear, forest, shock));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(shock);
        assertThat(gd.findExiledCard(bear.getId())).isNotNull();
        assertThat(gd.findExiledCard(forest.getId())).isNotNull();
        assertThat(gd.exiledCardsWithCroakCounters)
                .containsExactlyInAnyOrder(bear.getId(), forest.getId());
    }

    @Test
    @DisplayName("An attack by a non-Frog does not mill")
    void nonFrogAttackDoesNotMill() {
        addCreatureReady(player1, new GrolnokTheOmnivore());
        addCreatureReady(player1, new GrizzlyBears());
        List<Card> library = List.of(new GrizzlyBears(), new Forest(), new Shock());
        harness.setLibrary(player1, library);

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCardsWithCroakCounters).isEmpty();
    }

    @Test
    @DisplayName("The controller may cast a croak-counter spell and play a croak-counter land")
    void controllerMayCastAndPlayCroakCounterCards() {
        harness.addToBattlefield(player1, new GrolnokTheOmnivore());
        Card bear = new GrizzlyBears();
        Card forest = new Forest();
        harness.setExile(player1, List.of(bear, forest));
        gd.exiledCardsWithCroakCounters.add(bear.getId());
        gd.exiledCardsWithCroakCounters.add(forest.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromExile(player1, bear.getId());
        harness.passBothPriorities();
        harness.castFromExile(player1, forest.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(forest.getId()));
        assertThat(gd.findExiledCard(bear.getId())).isNull();
        assertThat(gd.findExiledCard(forest.getId())).isNull();
        assertThat(gd.exiledCardsWithCroakCounters).isEmpty();
    }

    @Test
    @DisplayName("A player cannot cast another player's croak-counter card")
    void cannotCastAnotherPlayersCroakCounterCard() {
        harness.addToBattlefield(player1, new GrolnokTheOmnivore());
        Card bear = new GrizzlyBears();
        harness.setExile(player1, List.of(bear));
        gd.exiledCardsWithCroakCounters.add(bear.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player2, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
