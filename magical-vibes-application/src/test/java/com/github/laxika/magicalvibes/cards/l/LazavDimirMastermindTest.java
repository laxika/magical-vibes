package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BalustradeSpy;
import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.c.Cremate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KillingGlare;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.t.TomeScour;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LazavDimirMastermind.class, GrizzlyBears.class, Ornithopter.class, TomeScour.class,
        BalustradeSpy.class, Clone.class, Cremate.class, KillingGlare.class, MindRot.class})
class LazavDimirMastermindTest extends BaseCardTest {

    private Permanent lazav() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard() instanceof LazavDimirMastermind)
                .findFirst()
                .orElseThrow();
    }

    private void millCreature(com.github.laxika.magicalvibes.model.Card creature) {
        harness.setLibrary(player2, List.of(creature, new TomeScour(), new TomeScour(),
                new TomeScour(), new TomeScour()));
        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Lazav's trigger resolves, queueing the may prompt
    }

    @Test
    @DisplayName("Becomes a copy of a creature card milled into an opponent's graveyard, keeping its name, legendary, and hexproof")
    void becomesCopyOfMilledCreature() {
        harness.addToBattlefield(player1, new LazavDimirMastermind());

        millCreature(new GrizzlyBears());
        harness.handleMayAbilityChosen(player1, true);

        var card = lazav().getCard();
        assertThat(card.getName()).isEqualTo("Lazav, Dimir Mastermind");
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(2);
        assertThat(card.getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(card.getKeywords()).contains(Keyword.HEXPROOF);
    }

    @Test
    @DisplayName("Declining the may choice leaves Lazav unchanged")
    void decliningLeavesLazavUnchanged() {
        harness.addToBattlefield(player1, new LazavDimirMastermind());

        millCreature(new GrizzlyBears());
        harness.handleMayAbilityChosen(player1, false);

        var card = lazav().getCard();
        assertThat(card.getPower()).isEqualTo(3);
        assertThat(card.getToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Keeps this ability after copying, so it can copy again")
    void keepsAbilityAfterCopying() {
        harness.addToBattlefield(player1, new LazavDimirMastermind());

        millCreature(new GrizzlyBears());
        harness.handleMayAbilityChosen(player1, true);

        millCreature(new Ornithopter());
        harness.handleMayAbilityChosen(player1, true);

        var card = lazav().getCard();
        assertThat(card.getName()).isEqualTo("Lazav, Dimir Mastermind");
        assertThat(card.getPower()).isZero();
        assertThat(card.getToughness()).isEqualTo(2);
        assertThat(card.getKeywords()).contains(Keyword.HEXPROOF, Keyword.FLYING);
    }

    @Test
    @DisplayName("Does not trigger when a creature card is put into the controller's own graveyard")
    void doesNotTriggerForOwnGraveyard() {
        harness.addToBattlefield(player1, new LazavDimirMastermind());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new TomeScour(), new TomeScour(),
                new TomeScour(), new TomeScour()));

        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.stack).isEmpty();
        var card = lazav().getCard();
        assertThat(card.getPower()).isEqualTo(3);
        assertThat(card.getToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not trigger for noncreature cards milled into an opponent's graveyard")
    void doesNotTriggerForNoncreatureCards() {
        harness.addToBattlefield(player1, new LazavDimirMastermind());
        harness.setLibrary(player2, List.of(new TomeScour(), new TomeScour(), new TomeScour(),
                new TomeScour(), new TomeScour()));
        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Can copy a creature card discarded from an opponent's hand")
    void copiesDiscardedCreature() {
        harness.addToBattlefield(player1, new LazavDimirMastermind());
        harness.setHand(player2, List.of(new GrizzlyBears(), new TomeScour()));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(lazav().getCard().getPower()).isEqualTo(2);
        assertThat(lazav().getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Copies a creature card that dies into an opponent's graveyard")
    void copiesDestroyedCreature() {
        harness.addToBattlefield(player1, new LazavDimirMastermind());
        Permanent spy = harness.addToBattlefieldAndReturn(player2, new BalustradeSpy());
        harness.setHand(player1, List.of(new KillingGlare()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, 2, spy.getId());
        harness.passBothPriorities();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(lazav().getCard().getPower()).isEqualTo(2);
        assertThat(lazav().getCard().getToughness()).isEqualTo(3);
        assertThat(lazav().getCard().getKeywords()).contains(Keyword.FLYING, Keyword.HEXPROOF);
    }

    @Test
    @DisplayName("Becoming a copy does not trigger the copied creature's enters ability")
    void copyingDoesNotTriggerEntersAbility() {
        harness.addToBattlefield(player1, new LazavDimirMastermind());
        millCreature(new BalustradeSpy());
        TomeScour remaining = new TomeScour();
        harness.setLibrary(player2, List.of(remaining));

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(lazav().getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Can copy the last known graveyard characteristics after the card is exiled")
    void copiesCardExiledInResponse() {
        harness.addToBattlefield(player1, new LazavDimirMastermind());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player2, List.of(bears, new TomeScour(), new TomeScour(),
                new TomeScour(), new TomeScour()));
        harness.setLibrary(player1, List.of(new TomeScour()));
        harness.setHand(player1, List.of(new TomeScour(), new Cremate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(lazav().getCard().getPower()).isEqualTo(2);
        assertThat(lazav().getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Copies the printed Clone card in the graveyard rather than its battlefield copy")
    void copyingDeadCloneMakesLazavZeroToughness() {
        harness.addToBattlefield(player1, new LazavDimirMastermind());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, bears.getId());
        Permanent clone = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getOriginalCard() instanceof Clone).findFirst().orElseThrow();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new KillingGlare()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, 2, clone.getId());
        harness.passBothPriorities();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Lazav, Dimir Mastermind");
        harness.assertInGraveyard(player1, "Lazav, Dimir Mastermind");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent cloneOpponentLazav() {
        Permanent opponentLazav = harness.addToBattlefieldAndReturn(player2, new LazavDimirMastermind());
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, opponentLazav.getId());
        return findPermanent(player1, "Lazav, Dimir Mastermind");
    }

    @Test
    @DisplayName("A Clone copying Lazav uses Lazav's required name after copying a graveyard card")
    void cloneKeepsLazavNameAfterGraveyardCopy() {
        Permanent clone = cloneOpponentLazav();

        millCreature(new GrizzlyBears());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(clone.getCard().getName()).isEqualTo("Lazav, Dimir Mastermind");
        assertThat(clone.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(clone.getCard().getKeywords()).contains(Keyword.HEXPROOF);
    }

    @Test
    @DisplayName("A Clone copying Lazav retains the graveyard-copy trigger after using it")
    void cloneRetainsLazavAbilityAfterGraveyardCopy() {
        Permanent clone = cloneOpponentLazav();
        millCreature(new GrizzlyBears());
        harness.handleMayAbilityChosen(player1, true);

        millCreature(new Ornithopter());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(clone.getCard().getPower()).isZero();
        assertThat(clone.getCard().getKeywords()).contains(Keyword.FLYING, Keyword.HEXPROOF);
    }
}
