package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.m.MarketwatchPhantom;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OnTheJob.class, MarketwatchPhantom.class})
class OnTheJobTest extends BaseCardTest {

    @Test
    void boostsOwnCreaturesAndCreatesAClue() {
        addCreatureReady(player1, new MarketwatchPhantom());
        addCreatureReady(player1, new MarketwatchPhantom());
        addCreatureReady(player2, new MarketwatchPhantom());
        castOnTheJob();

        assertThat(findPermanents(player1, "Marketwatch Phantom"))
                .allSatisfy(creature -> {
                    assertThat(creature.getEffectivePower()).isEqualTo(4);
                    assertThat(creature.getEffectiveToughness()).isEqualTo(3);
                });
        assertThat(findPermanents(player2, "Marketwatch Phantom"))
                .allSatisfy(creature -> {
                    assertThat(creature.getEffectivePower()).isEqualTo(2);
                    assertThat(creature.getEffectiveToughness()).isEqualTo(2);
                });
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void creatureBoostEndsAtCleanupButClueRemains() {
        addCreatureReady(player1, new MarketwatchPhantom());
        castOnTheJob();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent creature = findPermanent(player1, "Marketwatch Phantom");
        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void investigatesWithoutCreaturesAndClueCanBeSacrificedToDraw() {
        MarketwatchPhantom drawnCard = new MarketwatchPhantom();
        harness.setLibrary(player1, List.of(drawnCard));
        castOnTheJob();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
        Permanent clue = findPermanent(player1, "Clue");
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, clueIndex, null, null);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void creaturesEnteringAfterResolutionDoNotReceiveBoost() {
        Permanent original = addCreatureReady(player1, new MarketwatchPhantom());
        castOnTheJob();

        harness.castFromHand(player1, new MarketwatchPhantom(), "{1}{W}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(original.getEffectivePower()).isEqualTo(4);
        assertThat(original.getEffectiveToughness()).isEqualTo(3);
        assertThat(findPermanents(player1, "Marketwatch Phantom"))
                .hasSize(2)
                .filteredOn(creature -> creature != original)
                .singleElement()
                .satisfies(creature -> {
                    assertThat(creature.getEffectivePower()).isEqualTo(2);
                    assertThat(creature.getEffectiveToughness()).isEqualTo(2);
                });
    }

    private void castOnTheJob() {
        harness.castFromHand(player1, new OnTheJob(), "{2}{W}{W}");
        harness.passBothPriorities();
    }
}
