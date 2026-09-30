package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KithkinBillyrider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThoughtweftsCall.class, KithkinBillyrider.class, GrizzlyBears.class})
class ThoughtweftsCallTest extends BaseCardTest {

    @Test
    void seeksKithkinAndExilesItAtNextEndStepIfStillInHand() {
        Card kithkin = new KithkinBillyrider();
        harness.setLibrary(player1, List.of(kithkin, new GrizzlyBears()));
        castWithMode(0);

        harness.assertInHand(player1, "Kithkin Billyrider");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotInHand(player1, "Kithkin Billyrider");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(kithkin);
    }

    @Test
    void doesNotExileSoughtKithkinAfterItLeavesHand() {
        Card kithkin = new KithkinBillyrider();
        harness.setLibrary(player1, List.of(kithkin));
        castWithMode(0);
        harness.assertInHand(player1, "Kithkin Billyrider");

        gd.playerHands.get(player1.getId()).remove(kithkin);
        harness.setGraveyard(player1, List.of(kithkin));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(kithkin);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(kithkin);
    }

    @Test
    void perpetuallyBoostsCreatureCardsInHand() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(new ThoughtweftsCall(), bears));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent permanent = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(permanent.getEffectivePower()).isEqualTo(3);
        assertThat(permanent.getEffectiveToughness()).isEqualTo(3);
    }

    private void castWithMode(int modeIndex) {
        harness.setHand(player1, List.of(new ThoughtweftsCall()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castModalSorcery(player1, 0, modeIndex, List.of());
        harness.passBothPriorities();
    }
}
