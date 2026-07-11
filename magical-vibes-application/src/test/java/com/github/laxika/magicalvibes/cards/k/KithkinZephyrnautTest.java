package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KithkinZephyrnautTest extends BaseCardTest {

    @Test
    @DisplayName("Kinship prompts to reveal when the top card shares a creature type")
    void kinshipPromptsWhenSharedType() {
        addCreatureReady(player1, new KithkinZephyrnaut());
        setLibraryTop(new KithkinZephyrnaut()); // Kithkin Soldier — shares a type

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Revealing the shared-type card boosts and grants flying and vigilance")
    void revealBuffsSelf() {
        Permanent zephyrnaut = addCreatureReady(player1, new KithkinZephyrnaut());
        setLibraryTop(new KithkinZephyrnaut());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(zephyrnaut.getPowerModifier()).isEqualTo(2);
        assertThat(zephyrnaut.getToughnessModifier()).isEqualTo(2);
        assertThat(zephyrnaut.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(zephyrnaut.hasKeyword(Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("The Kinship boost wears off at cleanup")
    void buffWearsOffAtEndOfTurn() {
        Permanent zephyrnaut = addCreatureReady(player1, new KithkinZephyrnaut());
        setLibraryTop(new KithkinZephyrnaut());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(zephyrnaut.getPowerModifier()).isZero();
        assertThat(zephyrnaut.getToughnessModifier()).isZero();
        assertThat(zephyrnaut.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(zephyrnaut.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Declining to reveal leaves the creature unbuffed")
    void decliningDoesNothing() {
        Permanent zephyrnaut = addCreatureReady(player1, new KithkinZephyrnaut());
        setLibraryTop(new KithkinZephyrnaut());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(zephyrnaut.getPowerModifier()).isZero();
        assertThat(zephyrnaut.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("No reveal prompt when the top card shares no creature type")
    void noSharedTypeNoPrompt() {
        addCreatureReady(player1, new KithkinZephyrnaut());
        setLibraryTop(new GrizzlyBears()); // Bear — no shared type

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Trigger does nothing with an empty library")
    void emptyLibraryDoesNothing() {
        addCreatureReady(player1, new KithkinZephyrnaut());
        gd.playerDecks.get(player1.getId()).clear();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    private void setLibraryTop(Card card) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.add(card);
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
