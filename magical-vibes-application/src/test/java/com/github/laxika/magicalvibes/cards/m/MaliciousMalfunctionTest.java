package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaliciousMalfunction.class, GrizzlyBears.class, HillGiant.class})
class MaliciousMalfunctionTest extends BaseCardTest {

    @Test
    @DisplayName("Gives all creatures -2/-2 and exiles creatures that die this turn")
    void weakensAllCreaturesAndExilesThoseThatDie() {
        Permanent ownSurvivor = addCreatureReady(player1, new HillGiant());
        Permanent opponentSurvivor = addCreatureReady(player2, new HillGiant());
        Card ownDying = new GrizzlyBears();
        Card opponentDying = new GrizzlyBears();
        addCreatureReady(player1, ownDying);
        addCreatureReady(player2, opponentDying);
        harness.setHand(player1, List.of(new MaliciousMalfunction()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gqs.getEffectivePower(gd, ownSurvivor)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownSurvivor)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentSurvivor)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentSurvivor)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ownDying);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opponentDying);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(ownDying);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(opponentDying);
    }

    @Test
    @DisplayName("The debuff and replacement effect expire at end of turn")
    void effectsExpireAtEndOfTurn() {
        Card giantCard = new HillGiant();
        addCreatureReady(player2, giantCard);
        harness.setHand(player1, List.of(new MaliciousMalfunction()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent giant = findPermanent(player2, "Hill Giant");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, giant));

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(giantCard);
    }
}
