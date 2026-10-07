package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.k.KithkinZephyrnaut;
import com.github.laxika.magicalvibes.cards.m.MoongloveChangeling;
import com.github.laxika.magicalvibes.cards.m.Mutavault;
import com.github.laxika.magicalvibes.cards.r.RageForger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SqueakingPieGrubfellows.class, KithkinZephyrnaut.class, MoongloveChangeling.class,
        Mutavault.class, RageForger.class, ShardVolley.class})
class SqueakingPieGrubfellowsTest extends BaseCardTest {

    @Test
    @DisplayName("Kinship prompts to look when the top card shares a creature type")
    void kinshipPromptsWhenSharedType() {
        addCreatureReady(player1, new SqueakingPieGrubfellows());
        harness.setLibrary(player1, List.of(new SqueakingPieGrubfellows())); // Goblin Shaman — shares a type

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Looking and revealing the shared-type card are separate choices")
    void lookingAndRevealingAreSeparateChoices() {
        addCreatureReady(player1, new SqueakingPieGrubfellows());
        harness.setLibrary(player1, List.of(new SqueakingPieGrubfellows()));
        harness.setHand(player2, List.of(new KithkinZephyrnaut()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Revealing the shared-type card makes each opponent discard a card")
    void revealMakesOpponentDiscard() {
        addCreatureReady(player1, new SqueakingPieGrubfellows());
        harness.setLibrary(player1, List.of(new SqueakingPieGrubfellows()));
        harness.setHand(player2, List.of(new KithkinZephyrnaut(), new KithkinZephyrnaut()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining to reveal makes no opponent discard")
    void decliningDoesNothing() {
        addCreatureReady(player1, new SqueakingPieGrubfellows());
        harness.setLibrary(player1, List.of(new SqueakingPieGrubfellows()));
        harness.setHand(player2, List.of(new KithkinZephyrnaut()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Looking at a nonmatching top card offers no reveal choice")
    void noSharedTypeNoPrompt() {
        addCreatureReady(player1, new SqueakingPieGrubfellows());
        harness.setLibrary(player1, List.of(new KithkinZephyrnaut())); // Kithkin Soldier — no shared type

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Declining to look neither reveals nor makes an opponent discard")
    void decliningToLookDoesNothing() {
        addCreatureReady(player1, new SqueakingPieGrubfellows());
        SqueakingPieGrubfellows topCard = new SqueakingPieGrubfellows();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player2, List.of(new KithkinZephyrnaut()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Kinship with an empty library does not make an opponent discard")
    void emptyLibraryDoesNothing() {
        addCreatureReady(player1, new SqueakingPieGrubfellows());
        harness.setLibrary(player1, List.of());
        harness.setHand(player2, List.of(new KithkinZephyrnaut()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Sharing only Shaman is sufficient, and revealing leaves the card on top")
    void sharedShamanMakesOnlyOpponentDiscard() {
        addCreatureReady(player1, new SqueakingPieGrubfellows());
        RageForger topCard = new RageForger();
        harness.setLibrary(player1, List.of(topCard));
        KithkinZephyrnaut ownCard = new KithkinZephyrnaut();
        harness.setHand(player1, List.of(ownCard));
        harness.setHand(player2, List.of(new KithkinZephyrnaut(), new KithkinZephyrnaut()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ownCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("A Changeling on top shares a creature type with Grubfellows")
    void changelingMatches() {
        addCreatureReady(player1, new SqueakingPieGrubfellows());
        harness.setLibrary(player1, List.of(new MoongloveChangeling()));
        harness.setHand(player2, List.of(new KithkinZephyrnaut(), new KithkinZephyrnaut()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent with an empty hand cannot discard and needs no choice")
    void emptyOpponentHandNeedsNoChoice() {
        addCreatureReady(player1, new SqueakingPieGrubfellows());
        harness.setLibrary(player1, List.of(new SqueakingPieGrubfellows()));
        harness.setHand(player2, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Kinship does not trigger on an opponent's upkeep")
    void opponentUpkeepDoesNotTrigger() {
        addCreatureReady(player1, new SqueakingPieGrubfellows());
        harness.setLibrary(player1, List.of(new SqueakingPieGrubfellows()));
        harness.setHand(player2, List.of(new KithkinZephyrnaut()));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Kinship uses last known creature types when Grubfellows dies in response")
    void kinshipStillDiscardsAfterSourceDies() {
        Permanent grubfellows = addCreatureReady(player1, new SqueakingPieGrubfellows());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mutavault());
        SqueakingPieGrubfellows topCard = new SqueakingPieGrubfellows();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player2, List.of(new ShardVolley(), new KithkinZephyrnaut(), new KithkinZephyrnaut()));

        advanceToUpkeep(player1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstantWithSacrifice(player2, 0, grubfellows.getId(), land.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Squeaking Pie Grubfellows");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }
}
