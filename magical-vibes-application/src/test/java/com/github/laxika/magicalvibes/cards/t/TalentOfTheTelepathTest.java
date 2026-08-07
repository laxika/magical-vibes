package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TalentOfTheTelepathTest extends BaseCardTest {

    private void castTalentTargetingOpponent() {
        harness.setHand(player1, new ArrayList<>(List.of(new TalentOfTheTelepath())));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castInstant(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
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
        gd.playerGraveyards.get(player1.getId()).add(new Shock());
        gd.playerGraveyards.get(player1.getId()).add(new Shock());
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
    @DisplayName("Accepting casts a revealed sorcery for free; it never touches the graveyard")
    void castsRevealedSorceryForFree() {
        harness.setLibrary(player2, topSeven(new CounselOfTheSoratami()));

        castTalentTargetingOpponent();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve Counsel of the Soratami

        assertThat(gd.playerHands.get(player1.getId()).size() - handBefore).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Counsel of the Soratami"));
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(6);
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
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(6);
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
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(5);
    }

    @Test
    @DisplayName("No offer when none of the revealed cards is an instant or sorcery")
    void noOfferWithoutInstantOrSorcery() {
        harness.setLibrary(player2, topSeven());

        castTalentTargetingOpponent();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
