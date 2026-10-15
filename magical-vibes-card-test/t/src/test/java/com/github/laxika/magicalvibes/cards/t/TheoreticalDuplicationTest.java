package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.k.KrenkosCommand;
import com.github.laxika.magicalvibes.cards.b.BeastWithin;
import com.github.laxika.magicalvibes.cards.p.PygmyRazorback;
import com.github.laxika.magicalvibes.cards.r.RampagingBaloths;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheoreticalDuplication.class, PygmyRazorback.class, KrenkosCommand.class,
        BeastWithin.class, RampagingBaloths.class})
class TheoreticalDuplicationTest extends BaseCardTest {

    @Test
    void copiesNontokenCreaturesEnteringUnderAnOpponentsControl() {
        castDuplication();

        castPygmyRazorback(player2);

        assertThat(findPermanents(player2, "Pygmy Razorback")).hasSize(1);
        assertThat(findPermanents(player1, "Pygmy Razorback")).singleElement()
                .satisfies(copy -> assertThat(copy.getCard().isToken()).isTrue());
    }

    @Test
    void ignoresTokenCreaturesAndCreaturesEnteringUnderYourControl() {
        castDuplication();

        castPygmyRazorback(player1);
        castKrenkosCommand(player2);

        assertThat(findPermanents(player1, "Pygmy Razorback")).hasSize(1);
        assertThat(findPermanents(player1, "Goblin")).isEmpty();
    }

    @Test
    void expiresAtEndOfTurn() {
        castDuplication();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        castPygmyRazorback(player2);

        assertThat(findPermanents(player1, "Pygmy Razorback")).isEmpty();
    }

    @Test
    void copiesEveryQualifyingCreatureDuringTheTurn() {
        castDuplication();

        castPygmyRazorback(player2);
        castPygmyRazorback(player2);

        assertThat(findPermanents(player1, "Pygmy Razorback")).hasSize(2)
                .allSatisfy(copy -> assertThat(copy.getCard().isToken()).isTrue());
    }

    @Test
    void doesNotCopyCreaturesThatEnteredBeforeTheSpellResolved() {
        castPygmyRazorback(player2);

        castDuplication();

        assertThat(findPermanents(player1, "Pygmy Razorback")).isEmpty();
    }

    @Test
    void copiesCreatureUsingLastKnownInformationAfterItIsDestroyed() {
        castDuplication();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new RampagingBaloths(), "{4}{G}{G}");
        harness.passBothPriorities();

        var original = findPermanents(player2, "Rampaging Baloths").getFirst();
        harness.setHand(player1, List.of(new BeastWithin()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, original.getId());
        assertThat(findPermanents(player2, "Rampaging Baloths")).isEmpty();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Rampaging Baloths")).singleElement()
                .satisfies(copy -> assertThat(copy.getCard().isToken()).isTrue());
        assertThat(findPermanents(player1, "Beast")).isEmpty();
    }

    private void castDuplication() {
        harness.castFromHand(player1, new TheoreticalDuplication(), "{2}{U}");
        harness.passBothPriorities();
    }

    private void castPygmyRazorback(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player, new PygmyRazorback(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void castKrenkosCommand(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player, new KrenkosCommand(), "{1}{R}");
        harness.passBothPriorities();
    }
}
