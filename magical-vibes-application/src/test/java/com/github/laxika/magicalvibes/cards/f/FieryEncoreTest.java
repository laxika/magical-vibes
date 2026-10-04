package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DarettiScrapSavant;
import com.github.laxika.magicalvibes.cards.g.GuardianAugmenter;
import com.github.laxika.magicalvibes.cards.m.MyriadLandscape;
import com.github.laxika.magicalvibes.cards.s.SolemnSimulacrum;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FieryEncore.class, GuardianAugmenter.class, SolemnSimulacrum.class, MyriadLandscape.class,
        DarettiScrapSavant.class})
class FieryEncoreTest extends BaseCardTest {

    @Test
    @DisplayName("Discards, draws, and damages the target for the discarded nonland's mana value")
    void discardsDrawsAndDamagesForNonlandManaValue() {
        Permanent target = addCreatureReady(player2, new GuardianAugmenter());
        Card discarded = new SolemnSimulacrum();
        Card drawn = new MyriadLandscape();
        Card spell = new FieryEncore();
        harness.setHand(player1, List.of(spell, discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(target.getMarkedDamage()).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(spell, discarded);
        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertNotOnBattlefield(player2, "Guardian Augmenter");
    }

    @Test
    @DisplayName("A land discarded this way is replaced by a draw without dealing damage")
    void landDiscardDoesNotDamage() {
        Permanent target = addCreatureReady(player2, new GuardianAugmenter());
        Card discarded = new MyriadLandscape();
        Card drawn = new MyriadLandscape();
        harness.setHand(player1, List.of(new FieryEncore(), discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Storm creates one copy for each spell cast before Fiery Encore")
    void stormCopiesForEachPriorSpell() {
        gd.recordSpellCast(player1.getId(), new GuardianAugmenter());
        gd.recordSpellCast(player2.getId(), new GuardianAugmenter());
        harness.setHand(player1, List.of(new FieryEncore(), new SolemnSimulacrum(), new SolemnSimulacrum(), new SolemnSimulacrum()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
    }

    @Test
    @DisplayName("An empty hand still draws without any creature or planeswalker available")
    void emptyHandStillDrawsWithoutTargets() {
        Card drawn = new MyriadLandscape();
        harness.setHand(player1, List.of(new FieryEncore()));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Fiery Encore");
    }

    @Test
    @DisplayName("A nonland discard still draws when the damage trigger has no legal target")
    void nonlandDiscardWithoutTargetsStillDraws() {
        Card discarded = new SolemnSimulacrum();
        Card drawn = new MyriadLandscape();
        harness.setHand(player1, List.of(new FieryEncore(), discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The reflexive damage ability can destroy a planeswalker")
    void damagesPlaneswalkerAfterDiscard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DarettiScrapSavant());
        harness.setHand(player1, List.of(new FieryEncore(), new SolemnSimulacrum()));
        harness.setLibrary(player1, List.of(new MyriadLandscape()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Daretti, Scrap Savant");
        harness.assertInGraveyard(player2, "Daretti, Scrap Savant");
    }

    @Test
    @DisplayName("Every storm copy and the original discard and draw independently")
    void stormCopiesEachDiscardAndDraw() {
        gd.recordSpellCast(player1.getId(), new GuardianAugmenter());
        gd.recordSpellCast(player2.getId(), new GuardianAugmenter());
        Card firstDiscard = new MyriadLandscape();
        Card secondDiscard = new MyriadLandscape();
        Card thirdDiscard = new MyriadLandscape();
        Card firstDraw = new MyriadLandscape();
        Card secondDraw = new MyriadLandscape();
        Card thirdDraw = new MyriadLandscape();
        Card spell = new FieryEncore();
        harness.setHand(player1, List.of(spell, firstDiscard, secondDiscard, thirdDiscard));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, thirdDraw));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, List.of());
        for (int i = 0; i < 3; i++) {
            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw, thirdDraw);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(spell, firstDiscard, secondDiscard, thirdDiscard);
        assertThat(gd.stack).isEmpty();
    }
}
