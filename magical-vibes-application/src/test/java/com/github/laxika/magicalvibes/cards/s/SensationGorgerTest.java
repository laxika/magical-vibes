package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.k.KithkinZephyrnaut;
import com.github.laxika.magicalvibes.cards.r.RageForger;
import com.github.laxika.magicalvibes.cards.t.TaureanMauler;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SensationGorger.class, KithkinZephyrnaut.class, RageForger.class,
        TaureanMauler.class, Conspiracy.class})
class SensationGorgerTest extends BaseCardTest {

    @Test
    @DisplayName("Kinship prompts to reveal when the top card shares a creature type")
    void kinshipPromptsWhenSharedType() {
        addCreatureReady(player1, new SensationGorger());
        setLibraryTop(new SensationGorger()); // Goblin Shaman — shares a type

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Revealing makes each player discard their hand and draw four cards")
    void revealDiscardsHandsAndDrawsFour() {
        addCreatureReady(player1, new SensationGorger());
        setLibraryTop(new SensationGorger());
        harness.setHand(player1, List.of(new SensationGorger(), new SensationGorger()));
        harness.setHand(player2, List.of(new SensationGorger()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining to reveal leaves hands untouched")
    void decliningDoesNothing() {
        addCreatureReady(player1, new SensationGorger());
        setLibraryTop(new SensationGorger());
        harness.setHand(player1, List.of(new SensationGorger(), new SensationGorger()));
        harness.setHand(player2, List.of(new SensationGorger()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("No reveal prompt when the top card shares no creature type")
    void noSharedTypeNoPrompt() {
        addCreatureReady(player1, new SensationGorger());
        setLibraryTop(new KithkinZephyrnaut()); // Kithkin Soldier — no shared type

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("An empty library produces no reveal prompt")
    void emptyLibraryDoesNothing() {
        addCreatureReady(player1, new SensationGorger());
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Kinship uses the source's last known creature types if it leaves before resolution")
    void usesLastKnownSourceAfterLeavingBattlefield() {
        Permanent source = addCreatureReady(player1, new SensationGorger());
        setLibraryTop(new SensationGorger());

        advanceToUpkeep(player1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, source));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Kinship's accepted reveal still resolves after the source leaves")
    void acceptedRevealResolvesAfterSourceLeaves() {
        Permanent source = addCreatureReady(player1, new SensationGorger());
        setLibraryTop(new SensationGorger());
        harness.setHand(player1, List.of(new SensationGorger(), new SensationGorger()));
        harness.setHand(player2, List.of(new SensationGorger()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, source));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Sharing only Shaman is sufficient for kinship")
    void sharingOnlyShamanAllowsReveal() {
        addCreatureReady(player1, new SensationGorger());
        setLibraryTop(new RageForger());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("A changeling card in the library shares a creature type")
    void changelingAllowsReveal() {
        addCreatureReady(player1, new SensationGorger());
        setLibraryTop(new TaureanMauler());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("Players with empty hands still draw four, including the revealed top card")
    void emptyHandsStillDrawFourIncludingRevealedCard() {
        addCreatureReady(player1, new SensationGorger());
        RageForger revealedCard = new RageForger();
        setLibraryTop(revealedCard);
        harness.setLibrary(player2, List.of(new SensationGorger(), new SensationGorger(),
                new SensationGorger(), new SensationGorger(), new SensationGorger()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4).contains(revealedCard);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Kinship does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        addCreatureReady(player1, new SensationGorger());
        setLibraryTop(new SensationGorger());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @CardUsed({SensationGorger.class, KithkinZephyrnaut.class, Conspiracy.class})
    @DisplayName("Kinship compares creature types after continuous effects in both zones")
    void usesCreatureTypesGrantedByConspiracy() {
        addCreatureReady(player1, new SensationGorger());
        setLibraryTop(new KithkinZephyrnaut());
        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.ZOMBIE.name());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
    }

    private void setLibraryTop(Card card) {
        harness.setLibrary(player1, List.of(
                card,
                new SensationGorger(),
                new SensationGorger(),
                new SensationGorger(),
                new SensationGorger()));
    }
}
