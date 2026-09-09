package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShaleskinPlower.class, Forest.class, GrizzlyBears.class})
class ShaleskinPlowerTest extends BaseCardTest {

    @Test
    void turningFaceUpDestroysTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent plower = castFaceDown();

        turnFaceUp(plower);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(forest.getId());
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(plower.isFaceDown()).isFalse();
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void turningFaceUpOnlyOffersLandsAsTargets() {
        harness.addToBattlefield(player2, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent plower = castFaceDown();

        turnFaceUp(plower);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(bears.getId());
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new ShaleskinPlower()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanent(player1, "Shaleskin Plower");
    }

    private void turnFaceUp(Permanent plower) {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(plower));
    }
}
