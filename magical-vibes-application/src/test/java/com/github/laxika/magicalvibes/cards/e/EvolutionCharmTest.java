package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KavuPredator;
import com.github.laxika.magicalvibes.cards.s.SealOfPrimordium;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EvolutionCharm.class, Forest.class, KavuPredator.class, SealOfPrimordium.class})
class EvolutionCharmTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for a basic land and puts it into hand")
    void searchesForBasicLand() {
        Card forest = new Forest();
        Card creature = new KavuPredator();
        harness.setLibrary(player1, List.of(forest, creature));
        castCharm(0);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest);

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
    }

    @Test
    @DisplayName("Returns a target creature card from the graveyard to hand")
    void returnsCreatureFromGraveyard() {
        Card forest = new Forest();
        Card creature = new KavuPredator();
        harness.setGraveyard(player1, List.of(forest, creature));
        harness.setHand(player1, List.of(new EvolutionCharm()));
        addMana();

        harness.castInstant(player1, 0, 1, null);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Kavu Predator");
        harness.assertNotInGraveyard(player1, "Kavu Predator");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Creature-return mode requires a creature card in the graveyard")
    void creatureReturnModeRequiresTarget() {
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new EvolutionCharm()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gives target creature flying until end of turn")
    void givesFlyingUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KavuPredator());
        castCharm(2, target.getId());

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flying mode cannot target a noncreature permanent")
    void flyingModeCannotTargetNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SealOfPrimordium());
        harness.setHand(player1, List.of(new EvolutionCharm()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Basic-land search may fail to find even when a basic land is available")
    void mayDeclineToFindBasicLand() {
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        castCharm(0);

        harness.handleCardChosen(player1, -1);

        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        harness.assertInGraveyard(player1, "Evolution Charm");
    }

    @Test
    @DisplayName("Basic-land search resolves when the library has no basic lands")
    void resolvesWithoutMatchingBasicLand() {
        Card creature = new KavuPredator();
        harness.setLibrary(player1, List.of(creature));
        castCharm(0);

        harness.assertNotInHand(player1, "Kavu Predator");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        harness.assertInGraveyard(player1, "Evolution Charm");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Creature-return mode cannot use a creature in an opponent's graveyard")
    void cannotReturnOpponentsCreature() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new KavuPredator()));
        harness.setHand(player1, List.of(new EvolutionCharm()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player2, "Kavu Predator");
    }

    @Test
    @DisplayName("Creature-return mode does not return a target that leaves the graveyard")
    void doesNotReturnDepartedGraveyardTarget() {
        Card creature = new KavuPredator();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new EvolutionCharm()));
        addMana();
        harness.castInstant(player1, 0, 1, null);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Kavu Predator");
        harness.assertInGraveyard(player1, "Evolution Charm");
    }

    private void castCharm(int mode, UUID... targetIds) {
        harness.setHand(player1, List.of(new EvolutionCharm()));
        addMana();
        harness.castInstant(player1, 0, mode, targetIds.length == 0 ? null : targetIds[0]);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
    }

}
