package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GarrukUnleashed.class, GrizzlyBears.class, GarruksCompanion.class})
class GarrukUnleashedTest extends BaseCardTest {

    @Test
    @DisplayName("+1 boosts and grants trample to a target creature")
    void plusOneBoostsTargetCreature() {
        Permanent garruk = addReadyGarruk(player1, 4);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("+1 can be activated without choosing a creature")
    void plusOneCanHaveNoTarget() {
        Permanent garruk = addReadyGarruk(player1, 4);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("-2 creates a Beast and adds loyalty when an opponent still controls more creatures")
    void minusTwoCreatesBeastAndAddsLoyaltyWhenOpponentHasMoreCreatures() {
        Permanent garruk = addReadyGarruk(player1, 4);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GarruksCompanion());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        List<Permanent> beasts = findPermanents(player1, "Beast");
        assertThat(beasts).hasSize(1);
        Permanent beast = beasts.getFirst();
        assertThat(gqs.getEffectivePower(gd, beast)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, beast)).isEqualTo(3);
        assertThat(beast.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(beast.getCard().getSubtypes()).contains(CardSubtype.BEAST);
    }

    @Test
    @DisplayName("-2 checks creature counts after creating the Beast")
    void minusTwoDoesNotAddLoyaltyWhenTokenMakesCountsEqual() {
        Permanent garruk = addReadyGarruk(player1, 4);
        addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(findPermanents(player1, "Beast")).hasSize(1);
    }

    @Test
    @DisplayName("-7 emblem may search for a creature at the controller's end step")
    void minusSevenEmblemMaySearchAtEndStep() {
        Permanent garruk = addReadyGarruk(player1, 7);
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.emblems).hasSize(1);

        advanceIntoEndStep(player1);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(creature);

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The emblem does not trigger at the opponent's end step")
    void emblemDoesNotTriggerAtOpponentsEndStep() {
        addReadyGarruk(player1, 7);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        advanceIntoEndStep(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("+1 can boost an opponent's creature and wears off after the turn")
    void plusOneOnOpponentCreatureExpires() {
        addReadyGarruk(player1, 4);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("-2 still creates the Beast when paying loyalty removes Garruk")
    void minusTwoWithExactlyTwoLoyaltyStillCreatesToken() {
        addReadyGarruk(player1, 2);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GarruksCompanion());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Garruk, Unleashed")).isEmpty();
        harness.assertInGraveyard(player1, "Garruk, Unleashed");
        assertThat(findPermanents(player1, "Beast")).hasSize(1);
    }

    @Test
    @DisplayName("The emblem's search may be declined without moving library cards")
    void emblemSearchMayBeDeclined() {
        addReadyGarruk(player1, 7);
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        advanceIntoEndStep(player1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The emblem allows a creature search to find no card")
    void emblemSearchMayFailToFind() {
        addReadyGarruk(player1, 7);
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        advanceIntoEndStep(player1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("-2 does not put a counter on Garruk after he leaves and returns")
    void minusTwoDoesNotAddLoyaltyToReturnedGarruk() {
        Permanent original = addReadyGarruk(player1, 4);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GarruksCompanion());

        harness.activateAbility(player1, 0, 1, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(original);
        harness.setExile(player1, List.of(original.getCard()));
        harness.setExile(player1, List.of());
        Permanent returned = harness.addToBattlefieldAndReturn(player1, original.getCard());
        returned.setCounterCount(CounterType.LOYALTY, 4);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Beast")).hasSize(1);
        assertThat(returned.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    private Permanent addReadyGarruk(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new GarrukUnleashed());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private void advanceIntoEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
