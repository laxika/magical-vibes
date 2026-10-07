package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.FieryConclusion;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.ReaveSoul;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TalentOfTheTelepath.class, CounselOfTheSoratami.class, GrizzlyBears.class,
        Shock.class, FieryConclusion.class, ReaveSoul.class})
class TalentOfTheTelepathTest extends BaseCardTest {

    private void castTalentTargetingOpponent() {
        harness.setHand(player1, new ArrayList<>(List.of(new TalentOfTheTelepath())));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }

    private List<Card> topSeven(Card... top) {
        List<Card> library = new ArrayList<>(List.of(top));
        while (library.size() < 7) {
            library.add(new GrizzlyBears());
        }
        return library;
    }

    /** Two instants in the controller's graveyard turn spell mastery on. */
    private void enableSpellMastery() {
        harness.setGraveyard(player1, List.of(new Shock(), new Shock()));
    }

    @Test
    @DisplayName("Seven cards leave the opponent's library and the uncast rest hit their graveyard")
    void revealsSevenAndDumpsTheRest() {
        harness.setLibrary(player2, topSeven());
        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        castTalentTargetingOpponent();

        assertThat(deckBefore - gd.playerDecks.get(player2.getId()).size()).isEqualTo(7);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(7);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A revealed sorcery goes directly to the stack and resolves into its owner's graveyard")
    void castsRevealedSorceryForFree() {
        harness.setLibrary(player2, topSeven(new CounselOfTheSoratami()));

        castTalentTargetingOpponent();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertNotInGraveyard(player2, "Counsel of the Soratami");
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(6);
        harness.passBothPriorities(); // resolve Counsel of the Soratami

        assertThat(gd.playerHands.get(player1.getId()).size() - handBefore).isEqualTo(2);
        harness.assertInGraveyard(player2, "Counsel of the Soratami");
        harness.assertNotInGraveyard(player1, "Counsel of the Soratami");
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(7);
    }

    @Test
    @DisplayName("A revealed targeted instant is cast for free and prompts for its target")
    void castsRevealedTargetedInstant() {
        harness.setLibrary(player2, topSeven(new Shock()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        castTalentTargetingOpponent();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities(); // resolve Shock

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Declining every offer puts all revealed cards into the opponent's graveyard")
    void decliningDumpsEverything() {
        harness.setLibrary(player2, topSeven(new CounselOfTheSoratami()));

        castTalentTargetingOpponent();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Counsel of the Soratami"));
    }

    @Test
    @DisplayName("Without spell mastery only one revealed spell may be cast")
    void onlyOneSpellWithoutSpellMastery() {
        harness.setLibrary(player2, topSeven(new CounselOfTheSoratami(), new CounselOfTheSoratami()));

        castTalentTargetingOpponent();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId()).size() - handBefore).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(7);
    }

    @Test
    @DisplayName("Spell mastery lets a second revealed spell be cast")
    void spellMasteryCastsTwo() {
        harness.setLibrary(player2, topSeven(new CounselOfTheSoratami(), new CounselOfTheSoratami()));
        enableSpellMastery();

        castTalentTargetingOpponent();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size() - handBefore).isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(7);
    }

    @Test
    @DisplayName("No offer when none of the revealed cards is an instant or sorcery")
    void noOfferWithoutInstantOrSorcery() {
        harness.setLibrary(player2, topSeven());

        castTalentTargetingOpponent();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A mandatory sacrifice cost cannot be waived by casting for free")
    void cannotCastFieryConclusionWithoutCreatureToSacrifice() {
        harness.setLibrary(player2, topSeven(new FieryConclusion()));
        harness.addToBattlefield(player2, new GrizzlyBears());

        castTalentTargetingOpponent();

        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Fiery Conclusion");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Attempting an uncastable spell does not spend the allowed cast")
    void uncastableSpellDoesNotPreventCastingAnotherRevealedSpell() {
        harness.setLibrary(player2, topSeven(new ReaveSoul(), new CounselOfTheSoratami()));

        castTalentTargetingOpponent();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        harness.assertInGraveyard(player2, "Reave Soul");
    }

    @Test
    @DisplayName("A library with fewer than seven cards reveals only the available cards")
    void shortLibraryDoesNotDrawOrLoseTheGame() {
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        castTalentTargetingOpponent();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Talent itself does not count for spell mastery while resolving")
    void oneGraveyardSpellDoesNotEnableSpellMastery() {
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setLibrary(player2, topSeven(new CounselOfTheSoratami(), new CounselOfTheSoratami()));

        castTalentTargetingOpponent();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("Spell mastery permits declining the second spell")
    void spellMasteryMayCastOnlyOne() {
        enableSpellMastery();
        harness.setLibrary(player2, topSeven(new CounselOfTheSoratami(), new CounselOfTheSoratami()));

        castTalentTargetingOpponent();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
