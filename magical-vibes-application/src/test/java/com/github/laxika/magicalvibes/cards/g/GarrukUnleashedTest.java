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
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(creature);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.emblems).hasSize(1);

        advanceIntoEndStep(player1);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(creature);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

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

    private Permanent addReadyGarruk(Player player, int loyalty) {
        Permanent perm = new Permanent(new GarrukUnleashed());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private void advanceIntoEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
