package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CryOfTheCarnariumTest extends BaseCardTest {

    @Test
    @DisplayName("Gives creatures -2/-2 and exiles creatures that die this turn")
    void weakensCreaturesAndExilesThoseThatDie() {
        Permanent giant = addCreatureReady(player2, new HillGiant());
        Card bears = new GrizzlyBears();
        addCreatureReady(player2, bears);
        harness.setHand(player1, List.of(new CryOfTheCarnarium()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(bears);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("Exiles only creature cards put into graveyards from the battlefield this turn")
    void exilesOnlyRecentBattlefieldCreatureCards() {
        Card olderCreature = new GrizzlyBears();
        Card noncreature = new Shock();
        harness.setGraveyard(player2, List.of(olderCreature, noncreature));
        Card recentCreature = new GrizzlyBears();
        Permanent recentPermanent = addCreatureReady(player2, recentCreature);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, recentPermanent));

        harness.setHand(player1, List.of(new CryOfTheCarnarium()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(recentCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(olderCreature, noncreature);
    }

    @Test
    @DisplayName("The replacement effect expires at end of turn")
    void replacementEffectExpiresAtEndOfTurn() {
        Card giantCard = new HillGiant();
        addCreatureReady(player2, giantCard);
        harness.setHand(player1, List.of(new CryOfTheCarnarium()));
        harness.addMana(player1, ManaColor.BLACK, 3);

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
