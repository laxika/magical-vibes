package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RionyaFireDancer.class, GrizzlyBears.class, Shock.class})
class RionyaFireDancerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one hasty token copy when no instant or sorcery was cast")
    void createsOneTokenWithNoSpellsCast() {
        Permanent rionya = addCreatureReady(player1, new RionyaFireDancer());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);
        chooseTarget(target);

        List<Permanent> tokens = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().getKeywords()).contains(Keyword.HASTE);
        assertThat(rionya).isIn(gd.playerBattlefields.get(player1.getId()));
    }

    @Test
    @DisplayName("Creates one additional token for each instant or sorcery cast this turn")
    void countsInstantAndSorcerySpellsCastThisTurn() {
        addCreatureReady(player1, new RionyaFireDancer());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        advanceToBeginningOfCombat(player1);
        chooseTarget(target);

        assertThat(findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken()))
                .hasSize(3);
    }

    @Test
    @DisplayName("Targets only another creature controlled by Rionya's controller")
    void targetsOnlyAnotherControlledCreature() {
        Permanent rionya = addCreatureReady(player1, new RionyaFireDancer());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(ownCreature.getId())
                .doesNotContain(rionya.getId(), opponentCreature.getId());
    }

    @Test
    @DisplayName("Exiles the token copies at the beginning of the next end step")
    void exilesCopiesAtNextEndStep() {
        addCreatureReady(player1, new RionyaFireDancer());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);
        chooseTarget(target);
        assertThat(findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())).isEmpty();
    }

    private void chooseTarget(Permanent target) {
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
