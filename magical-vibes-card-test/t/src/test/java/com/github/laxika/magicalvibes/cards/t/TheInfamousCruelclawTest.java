package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.d.DazzlingDenial;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheInfamousCruelclaw.class, CounselOfTheSoratami.class, DazzlingDenial.class,
        Forest.class, GrizzlyBears.class})
class TheInfamousCruelclawTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles until a nonland card and casts it by discarding a card")
    void castsExiledNonlandByDiscarding() {
        CounselOfTheSoratami spell = new CounselOfTheSoratami();
        Forest land = new Forest();
        GrizzlyBears discarded = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land, spell, new Forest(), new Forest()));
        harness.setHand(player1, List.of(discarded));

        attackWithCruelclaw();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(land, spell);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(discarded.getId()));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(land);
    }

    @Test
    @DisplayName("Declining leaves the nonland card and intermediate lands in exile")
    void decliningLeavesCardsInExile() {
        CounselOfTheSoratami spell = new CounselOfTheSoratami();
        Forest land = new Forest();
        GrizzlyBears discarded = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land, spell));
        harness.setHand(player1, List.of(discarded));

        attackWithCruelclaw();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(land, spell);
    }

    @Test
    @DisplayName("Does not offer the cast when the controller has no card to discard")
    void noDiscardAvailableLeavesCardsInExile() {
        CounselOfTheSoratami spell = new CounselOfTheSoratami();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land, spell));
        harness.setHand(player1, List.of());

        attackWithCruelclaw();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(land, spell);
    }

    @Test
    @DisplayName("The exiled creature is cast during the original trigger's resolution")
    void castsDuringOriginalResolution() {
        TheInfamousCruelclaw spell = new TheInfamousCruelclaw();
        Forest discarded = new Forest();
        harness.setLibrary(player1, List.of(spell));
        harness.setHand(player1, List.of(discarded));

        attackWithCruelclaw();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(spell.getId());
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("An uncastable counterspell does not consume the discard cost")
    void noLegalTargetsDoesNotDiscard() {
        DazzlingDenial spell = new DazzlingDenial();
        Forest discarded = new Forest();
        harness.setLibrary(player1, List.of(spell));
        harness.setHand(player1, List.of(discarded));

        attackWithCruelclaw();

        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }
        if (gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class) != null) {
            harness.handleCardChosen(player1, 0);
        }
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A library containing only lands is entirely exiled without offering a cast")
    void allLandLibraryIsExiled() {
        Forest first = new Forest();
        Forest second = new Forest();
        Forest handCard = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(handCard));

        attackWithCruelclaw();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library does not offer a cast or discard")
    void emptyLibraryDoesNothing() {
        Forest handCard = new Forest();
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(handCard));

        attackWithCruelclaw();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void attackWithCruelclaw() {
        Permanent cruelclaw = addCreatureReady(player1, new TheInfamousCruelclaw());
        cruelclaw.setAttacking(true);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }
}
