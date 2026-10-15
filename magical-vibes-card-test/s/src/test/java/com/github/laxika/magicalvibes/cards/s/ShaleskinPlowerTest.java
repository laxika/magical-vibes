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

    @Test
    void turningFaceUpMustDestroyYourOwnLandWhenItIsTheOnlyTarget() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent plower = castFaceDown();

        turnFaceUp(plower);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(forest.getId());
        harness.handlePermanentChosen(player1, forest.getId());
        assertThat(gd.stack).hasSize(1);
        harness.assertOnBattlefield(player1, "Forest");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player1, "Shaleskin Plower");
    }

    @Test
    void turningFaceUpWithoutLandsStillTurnsTheCreatureFaceUp() {
        Permanent plower = castFaceDown();

        turnFaceUp(plower);

        assertThat(plower.isFaceDown()).isFalse();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Shaleskin Plower");
    }

    @Test
    void castingFaceUpDoesNotDestroyALand() {
        harness.addToBattlefield(player2, new Forest());
        harness.castFromHand(player1, new ShaleskinPlower(), "{3}{R}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shaleskin Plower");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertNotInGraveyard(player2, "Forest");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
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
