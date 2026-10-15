package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BallyrushBanneret;
import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.s.StonybrookBanneret;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NightshadeSchemers.class, BallyrushBanneret.class, StonybrookBanneret.class, Disperse.class})
class NightshadeSchemersTest extends BaseCardTest {

    @Test
    @DisplayName("Kinship prompts to reveal when the top card shares a creature type")
    void kinshipPromptsWhenSharedType() {
        addCreatureReady(player1, new NightshadeSchemers());
        harness.setLibrary(player1, List.of(new NightshadeSchemers())); // Faerie Wizard — shares a type

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Revealing the shared-type card makes each opponent lose 2 life")
    void revealDrainsOpponent() {
        addCreatureReady(player1, new NightshadeSchemers());
        NightshadeSchemers topCard = new NightshadeSchemers();
        harness.setLibrary(player1, List.of(topCard));

        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Declining to reveal loses no life")
    void decliningDoesNothing() {
        addCreatureReady(player1, new NightshadeSchemers());
        NightshadeSchemers topCard = new NightshadeSchemers();
        harness.setLibrary(player1, List.of(topCard));

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("No reveal prompt when the top card shares no creature type")
    void noSharedTypeNoPrompt() {
        addCreatureReady(player1, new NightshadeSchemers());
        harness.setLibrary(player1, List.of(new BallyrushBanneret())); // Kithkin Soldier — no shared type

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Kinship recognizes a card sharing only one of the source's creature types")
    void sharedSingleTypePrompts() {
        addCreatureReady(player1, new NightshadeSchemers());
        harness.setLibrary(player1, List.of(new StonybrookBanneret())); // Merfolk Wizard — shares Wizard

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Trigger does nothing with an empty library")
    void emptyLibraryDoesNothing() {
        addCreatureReady(player1, new NightshadeSchemers());
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Kinship does not trigger during an opponent's upkeep")
    void opponentUpkeepDoesNotTrigger() {
        addCreatureReady(player1, new NightshadeSchemers());
        harness.setLibrary(player1, List.of(new NightshadeSchemers()));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Kinship uses last known creature types after its source is returned to hand")
    void kinshipResolvesAfterSourceLeavesBattlefield() {
        var source = addCreatureReady(player1, new NightshadeSchemers());
        StonybrookBanneret topCard = new StonybrookBanneret();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player2, List.of(new Disperse()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        advanceToUpkeep(player1);
        harness.castInstant(player2, 0, source.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Nightshade Schemers");
        harness.assertInHand(player1, "Nightshade Schemers");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }
}
