package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IndoctrinationAttendantTest extends BaseCardTest {

    private void castAndResolve() {
        harness.setHand(player1, List.of(new IndoctrinationAttendant()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Returning another permanent creates a toxic Mite")
    void returningAnotherPermanentCreatesMite() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(bears.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertInHand(player1, "Grizzly Bears");
        Permanent mite = findPermanent(player1, "Mite");
        assertThat(mite.getCard().getKeywords()).contains(Keyword.TOXIC);
        assertThat(bls.canBlock(gd, mite)).isFalse();
    }

    @Test
    @DisplayName("Declining the return leaves the battlefield unchanged")
    void decliningReturnDoesNothing() {
        addCreatureReady(player1, new GrizzlyBears());
        castAndResolve();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Mite")).isEmpty();
    }

    @Test
    @DisplayName("No Mite is created when no other permanent can be returned")
    void noOtherPermanentMeansNoMite() {
        castAndResolve();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Indoctrination Attendant");
        assertThat(findPermanents(player1, "Mite")).isEmpty();
    }
}
