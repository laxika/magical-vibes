package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BurningInquiry;
import com.github.laxika.magicalvibes.cards.c.CarapaceForger;
import com.github.laxika.magicalvibes.cards.c.Catalog;
import com.github.laxika.magicalvibes.cards.c.CollectiveBrutality;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.n.NullProfusion;
import com.github.laxika.magicalvibes.cards.p.PainfulQuandary;
import com.github.laxika.magicalvibes.cards.p.PossessedPortal;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WildMongrel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LibraryOfLeng.class, BurningInquiry.class, Catalog.class, GrizzlyBears.class, Island.class, NullProfusion.class,
        WildMongrel.class, LurchingRotbeast.class, CollectiveBrutality.class, Shock.class, PainfulQuandary.class,
        CarapaceForger.class, Memnite.class, PossessedPortal.class})
class LibraryOfLengTest extends BaseCardTest {

    @Test
    @DisplayName("A chosen discard by the controller goes on top of their library instead of the graveyard")
    void chosenDiscardGoesOnTopOfLibrary() {
        harness.addToBattlefield(player1, new LibraryOfLeng());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new Catalog(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        // Catalog drew two Islands; hand is GrizzlyBears + 2 Islands. Discard the GrizzlyBears.
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        // The discarded Grizzly Bears is put on top of the library, not into the graveyard.
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Catalog");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    void controllerMayDeclineLibraryReplacement() {
        harness.addToBattlefield(player1, new LibraryOfLeng());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new Catalog(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof GrizzlyBears);
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(card -> card instanceof GrizzlyBears);
    }

    @Test
    void controllerHasNoMaximumHandSize() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.addToBattlefield(player1, new LibraryOfLeng());
        harness.setHand(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()
        ));

        harness.getGameService().advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
    }

    @Test
    @DisplayName("Random discards by the controller go on top of library; opponent without Leng still discards to graveyard")
    void randomDiscardRedirectOnlyForController() {
        harness.addToBattlefield(player1, new LibraryOfLeng());
        int p1DeckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new BurningInquiry()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        for (int i = 0; i < 3; i++) {
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
            harness.handleMayAbilityChosen(player1, true);
        }

        // Player 1 drew 3 and discarded 3 at random; with Library of Leng they go back on top of
        // the library, so the deck size is unchanged and only Burning Inquiry hits the graveyard.
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(p1DeckBefore);
        assertThat(gd.playerGraveyards.get(player1.getId())).singleElement()
                .matches(c -> c.getName().equals("Burning Inquiry"));

        // Player 2 has no Library of Leng — their 3 random discards go to the graveyard as normal.
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    void randomDiscardReplacementMayBeDeclinedForEveryCard() {
        harness.addToBattlefield(player1, new LibraryOfLeng());
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island(), new Island()));
        harness.setLibrary(player2, List.of(new Island(), new Island(), new Island(), new Island()));
        harness.setHand(player1, List.of(new BurningInquiry()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        for (int i = 0; i < 3; i++) {
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
            harness.handleMayAbilityChosen(player1, false);
        }

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    void laterMaximumHandSizeEffectOverridesLibraryOfLeng() {
        harness.addToBattlefield(player1, new LibraryOfLeng());
        harness.addToBattlefield(player1, new NullProfusion());
        harness.setHand(player1, List.of(new Island(), new Island(), new Island()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
    }

    @Test
    void libraryOfLengOverridesEarlierMaximumHandSizeEffect() {
        harness.addToBattlefield(player1, new NullProfusion());
        harness.addToBattlefield(player1, new LibraryOfLeng());
        harness.setHand(player1, List.of(new Island(), new Island(), new Island()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void opponentStillHasMaximumHandSize() {
        harness.addToBattlefield(player1, new LibraryOfLeng());
        harness.setHand(player2, List.of(new Island(), new Island(), new Island(), new Island(),
                new Island(), new Island(), new Island(), new Island()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        harness.handleCardChosen(player2, 0);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        harness.assertInGraveyard(player2, "Island");
    }

    @Test
    void replacedDiscardDoesNotRevealCardInPublicLog() {
        harness.addToBattlefield(player1, new LibraryOfLeng());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new Catalog(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);
        int logSizeBeforeDiscard = gd.gameLog.size();
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.gameLog.subList(logSizeBeforeDiscard, gd.gameLog.size()))
                .noneMatch(entry -> entry.plainText().contains("Grizzly Bears"));
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(GrizzlyBears.class);
    }

    @Test
    @DisplayName("A card discarded to pay an activation cost goes to the graveyard, as costs aren't effects")
    void activationDiscardCostIsNotReplaced() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new WildMongrel());
        harness.addToBattlefield(player1, new LibraryOfLeng());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).noneMatch(card -> card instanceof GrizzlyBears);
    }

    @Test
    @DisplayName("A cycled card goes to the graveyard, as its discard is part of the cycling cost")
    void cycledCardIsNotReplaced() {
        harness.addToBattlefield(player1, new LibraryOfLeng());
        LurchingRotbeast cycled = new LurchingRotbeast();
        harness.setHand(player1, List.of(cycled));
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(cycled);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(cycled);
    }

    @Test
    @DisplayName("A card discarded for escalate goes to the graveyard, as escalate is a cost")
    void escalateDiscardIsNotReplaced() {
        harness.addToBattlefield(player1, new LibraryOfLeng());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CollectiveBrutality(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{1, 2},
                List.of(bears.getId(), player2.getId()), List.of(1));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.playerDecks.get(player1.getId())).noneMatch(card -> card instanceof Shock);
    }

    @Test
    @DisplayName("A card discarded to pay an \"unless\" ransom goes to the graveyard, as the ransom is a cost")
    void unlessRansomDiscardIsNotReplaced() {
        harness.addToBattlefield(player1, new PainfulQuandary());
        harness.addToBattlefield(player2, new LibraryOfLeng());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CarapaceForger(), new Memnite()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Memnite");
        assertThat(gd.playerDecks.get(player2.getId())).noneMatch(card -> card instanceof Memnite);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A card discarded to pay a ransom paid together goes to the graveyard, as the ransom is a cost")
    void ransomPaidTogetherDiscardIsNotReplaced() {
        harness.addToBattlefield(player1, new PossessedPortal());
        harness.addToBattlefield(player1, new LibraryOfLeng());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Library of Leng");
        assertThat(gd.playerDecks.get(player1.getId())).noneMatch(card -> card instanceof GrizzlyBears);
    }
}
