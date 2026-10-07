package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AncestralReminiscence;
import com.github.laxika.magicalvibes.cards.d.DreamTwist;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MemorysJourney;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UchbenbakTheGreatMistake.class, Forest.class, AncestralReminiscence.class, MemorysJourney.class, DreamTwist.class})
class UchbenbakTheGreatMistakeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns from the graveyard with a finality counter when descended eight")
    void returnsFromGraveyardWithFinalityCounter() {
        prepareMainPhase();
        Card uchbenbak = new UchbenbakTheGreatMistake();
        harness.setGraveyard(player1, graveyardWithAdditionalPermanents(uchbenbak, 7));
        addActivationMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Uchbenbak, the Great Mistake");
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Uchbenbak, the Great Mistake");
    }

    @Test
    @DisplayName("Cannot activate without eight permanent cards in the graveyard")
    void cannotActivateWithoutEightPermanentCards() {
        prepareMainPhase();
        Card uchbenbak = new UchbenbakTheGreatMistake();
        harness.setGraveyard(player1, graveyardWithAdditionalPermanents(uchbenbak, 6));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Uchbenbak, the Great Mistake");
    }

    @Test
    @DisplayName("Cannot activate outside sorcery timing")
    void cannotActivateOutsideSorceryTiming() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        Card uchbenbak = new UchbenbakTheGreatMistake();
        harness.setGraveyard(player1, graveyardWithAdditionalPermanents(uchbenbak, 7));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sorceries do not count toward the eight permanent cards")
    void nonpermanentCardsDoNotMeetThreshold() {
        prepareMainPhase();
        Card uchbenbak = new UchbenbakTheGreatMistake();
        List<Card> graveyard = graveyardWithAdditionalPermanents(uchbenbak, 6);
        graveyard.add(new AncestralReminiscence());
        harness.setGraveyard(player1, graveyard);
        addActivationMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Uchbenbak, the Great Mistake");
    }

    @Test
    @DisplayName("Opponent's graveyard does not contribute to descend eight")
    void opponentsPermanentsDoNotMeetThreshold() {
        prepareMainPhase();
        Card uchbenbak = new UchbenbakTheGreatMistake();
        harness.setGraveyard(player1, graveyardWithAdditionalPermanents(uchbenbak, 6));
        harness.setGraveyard(player2, List.of(new Forest()));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate during the opponent's main phase")
    void cannotActivateDuringOpponentsMainPhase() {
        prepareMainPhase();
        harness.forceActivePlayer(player2);
        Card uchbenbak = new UchbenbakTheGreatMistake();
        harness.setGraveyard(player1, graveyardWithAdditionalPermanents(uchbenbak, 7));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate again while the first ability is on the stack")
    void cannotActivateWithNonemptyStack() {
        prepareMainPhase();
        Card uchbenbak = new UchbenbakTheGreatMistake();
        harness.setGraveyard(player1, graveyardWithAdditionalPermanents(uchbenbak, 7));
        addActivationMana();
        addActivationMana();
        harness.activateGraveyardAbility(player1, 0);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Uchbenbak, the Great Mistake");
    }

    @Test
    @DisplayName("Descend eight is not checked again at resolution")
    void returnsEvenIfThresholdIsNoLongerMet() {
        prepareMainPhase();
        Card uchbenbak = new UchbenbakTheGreatMistake();
        harness.setGraveyard(player1, graveyardWithAdditionalPermanents(uchbenbak, 7));
        addActivationMana();
        harness.activateGraveyardAbility(player1, 0);
        harness.setGraveyard(player1, List.of(uchbenbak));

        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Uchbenbak, the Great Mistake")
                .getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Uchbenbak, the Great Mistake");
    }

    @Test
    @DisplayName("Returns only the activated copy, leaving another copy in the graveyard")
    void returnsOnlyTheActivatedCopy() {
        prepareMainPhase();
        Card uchbenbak = new UchbenbakTheGreatMistake();
        Card otherCopy = new UchbenbakTheGreatMistake();
        List<Card> graveyard = graveyardWithAdditionalPermanents(uchbenbak, 6);
        graveyard.add(otherCopy);
        harness.setGraveyard(player1, graveyard);
        addActivationMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Uchbenbak, the Great Mistake").getCard().getId())
                .isEqualTo(uchbenbak.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(otherCopy).doesNotContain(uchbenbak);
    }

    @Test
    @DisplayName("Finality exiles the returned creature instead of allowing it to die")
    void finalityExilesInsteadOfDying() {
        prepareMainPhase();
        Card uchbenbak = new UchbenbakTheGreatMistake();
        harness.setGraveyard(player1, graveyardWithAdditionalPermanents(uchbenbak, 7));
        addActivationMana();
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        findPermanent(player1, "Uchbenbak, the Great Mistake").setMarkedDamage(4);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Uchbenbak, the Great Mistake");
        harness.assertNotInGraveyard(player1, "Uchbenbak, the Great Mistake");
        assertThat(gd.exiledCards).anySatisfy(entry ->
                assertThat(entry.card().getId()).isEqualTo(uchbenbak.getId()));
    }

    @Test
    @DisplayName("Cannot return a new object after the source leaves and reenters the graveyard")
    void doesNotReturnSourceAfterLibraryRoundTrip() {
        prepareMainPhase();
        Card uchbenbak = new UchbenbakTheGreatMistake();
        harness.setGraveyard(player1, graveyardWithAdditionalPermanents(uchbenbak, 7));
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new MemorysJourney(), new DreamTwist()));
        addActivationMana();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateGraveyardAbility(player1, 0);

        harness.castInstant(player1, 0, player1.getId());
        harness.handleMultipleCardsChosen(player1, List.of(uchbenbak.getId()));
        harness.passBothPriorities();
        harness.assertNotInGraveyard(player1, "Uchbenbak, the Great Mistake");
        assertThat(gd.stack).hasSize(1);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.assertInGraveyard(player1, "Uchbenbak, the Great Mistake");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Uchbenbak, the Great Mistake");
        harness.assertInGraveyard(player1, "Uchbenbak, the Great Mistake");
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private List<Card> graveyardWithAdditionalPermanents(Card source, int additionalPermanents) {
        List<Card> graveyard = new ArrayList<>();
        graveyard.add(source);
        for (int i = 0; i < additionalPermanents; i++) {
            graveyard.add(new Forest());
        }
        return graveyard;
    }
}
