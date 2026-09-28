package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.k.KrenkosCommand;
import com.github.laxika.magicalvibes.cards.p.PygmyRazorback;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheoreticalDuplication.class, PygmyRazorback.class, KrenkosCommand.class})
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

    private void castDuplication() {
        harness.setHand(player1, List.of(new TheoreticalDuplication()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private void castPygmyRazorback(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player, List.of(new PygmyRazorback()));
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void castKrenkosCommand(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player, List.of(new KrenkosCommand()));
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castSorcery(player, 0);
        harness.passBothPriorities();
    }
}
