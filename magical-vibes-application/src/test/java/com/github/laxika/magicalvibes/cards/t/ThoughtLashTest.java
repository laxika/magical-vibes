package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThoughtLashTest extends BaseCardTest {

    @Test
    @DisplayName("Paying cumulative upkeep exiles the top card and keeps Thought Lash")
    void payingUpkeepExilesTopCard() {
        Permanent lash = harness.addToBattlefieldAndReturn(player1, new ThoughtLash());

        Card topCard = gd.playerDecks.get(player1.getId()).getFirst();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(lash.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(lash);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.exiledCards).extracting(e -> e.card().getId()).contains(topCard.getId());
    }

    @Test
    @DisplayName("Second upkeep costs two cards from the top of the library")
    void secondUpkeepExilesTwoCards() {
        Permanent lash = harness.addToBattlefieldAndReturn(player1, new ThoughtLash());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        int deckAfterFirst = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(lash.getCounterCount(CounterType.AGE)).isEqualTo(2);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(lash);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckAfterFirst - 2);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Thought Lash and exiles the whole library")
    void decliningExilesEntireLibrary() {
        Permanent lash = harness.addToBattlefieldAndReturn(player1, new ThoughtLash());

        int deckBefore = gd.playerDecks.get(player1.getId()).size();
        assertThat(deckBefore).isPositive();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(lash);
        harness.assertInGraveyard(player1, "Thought Lash");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).hasSize(deckBefore);
    }

    @Test
    @DisplayName("An empty library makes the upkeep unpayable — sacrifice with no prompt")
    void emptyLibraryAutoSacrifices() {
        Permanent lash = harness.addToBattlefieldAndReturn(player1, new ThoughtLash());
        gd.playerDecks.get(player1.getId()).clear();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(lash);
        harness.assertInGraveyard(player1, "Thought Lash");
    }

    @Test
    @DisplayName("Activated ability exiles the top card and prevents the next 1 damage to controller")
    void abilityPreventsOneDamage() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ThoughtLash());

        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);

        // A 2/2 attacker deals 2 — one point is prevented, one gets through.
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }
}
