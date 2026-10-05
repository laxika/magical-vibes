package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AuntiesSnitch;
import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.v.VendilionClique;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InkDissolver.class, AuntiesSnitch.class, VendilionClique.class, Disperse.class})
class InkDissolverTest extends BaseCardTest {

    @Test
    @DisplayName("Kinship prompts to reveal when the top card shares a creature type")
    void kinshipPromptsWhenSharedType() {
        addCreatureReady(player1, new InkDissolver());
        harness.setLibrary(player1, List.of(new InkDissolver())); // Merfolk Wizard — shares both types

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Revealing the shared-type card mills three cards from each opponent")
    void revealMillsOpponent() {
        addCreatureReady(player1, new InkDissolver());
        InkDissolver topCard = new InkDissolver();
        harness.setLibrary(player1, List.of(topCard));

        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore - 3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining to reveal mills nothing")
    void decliningDoesNothing() {
        addCreatureReady(player1, new InkDissolver());
        harness.setLibrary(player1, List.of(new InkDissolver()));

        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("No reveal prompt when the top card shares no creature type")
    void noSharedTypeNoPrompt() {
        addCreatureReady(player1, new InkDissolver());
        harness.setLibrary(player1, List.of(new AuntiesSnitch())); // Goblin Rogue — no shared type

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Kinship recognizes a card sharing only one of the source's creature types")
    void sharedSingleTypePrompts() {
        addCreatureReady(player1, new InkDissolver());
        harness.setLibrary(player1, List.of(new VendilionClique())); // Faerie Wizard — shares Wizard

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Trigger does nothing with an empty library")
    void emptyLibraryDoesNothing() {
        addCreatureReady(player1, new InkDissolver());
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Kinship still reveals and mills after its source leaves the battlefield")
    void kinshipUsesLastKnownTypesAfterSourceLeaves() {
        var source = addCreatureReady(player1, new InkDissolver());
        InkDissolver topCard = new InkDissolver();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLibrary(player2, List.of(new AuntiesSnitch(), new AuntiesSnitch(), new AuntiesSnitch()));
        harness.setHand(player1, List.of(new Disperse()));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, source.getId());
        harness.assertNotOnBattlefield(player1, "Ink Dissolver");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("An opponent with fewer than three cards mills its entire library")
    void millsShortLibrary() {
        addCreatureReady(player1, new InkDissolver());
        harness.setLibrary(player1, List.of(new InkDissolver()));
        AuntiesSnitch first = new AuntiesSnitch();
        AuntiesSnitch second = new AuntiesSnitch();
        harness.setLibrary(player2, List.of(first, second));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("Kinship does not trigger during an opponent's upkeep")
    void opponentUpkeepDoesNotTrigger() {
        addCreatureReady(player1, new InkDissolver());
        harness.setLibrary(player1, List.of(new InkDissolver()));
        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
