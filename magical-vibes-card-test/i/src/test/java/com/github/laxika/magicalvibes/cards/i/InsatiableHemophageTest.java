package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({InsatiableHemophage.class})
class InsatiableHemophageTest extends BaseCardTest {

    @Test
    @DisplayName("Mutations drain life equal to the number of times Insatiable Hemophage mutated")
    void mutationsScaleLifeDrain() {
        Permanent hemophage = addCreatureReady(player1, new InsatiableHemophage());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        mutate(hemophage);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);

        mutate(hemophage);

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Casting normally does not trigger mutation life drain")
    void normalCastDoesNotDrainLife() {
        harness.castFromHand(player1, new InsatiableHemophage(), "{3}{B}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Insatiable Hemophage");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Insatiable Hemophage can be cast for its mutate cost targeting an owned non-Human")
    void canCastForMutateCost() {
        Permanent target = addCreatureReady(player1, new InsatiableHemophage());
        harness.setHand(player1, List.of(new InsatiableHemophage()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castWithAlternateCost(player1, 0, target.getId());
    }

    @Test
    @DisplayName("Pending mutation triggers use the mutation count at resolution")
    void pendingTriggersUseCurrentMutationCount() {
        Permanent hemophage = addCreatureReady(player1, new InsatiableHemophage());
        harness.inMutationScope(() -> {
            harness.getTriggerCollectionService().checkMutateTriggers(
                    gd, hemophage, List.of(hemophage.getCard()), player1.getId());
            harness.getTriggerCollectionService().checkMutateTriggers(
                    gd, hemophage, List.of(hemophage.getCard()), player1.getId());
        });

        resolveAllTriggers();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Mutation drain still resolves after the source leaves the battlefield")
    void drainUsesLastKnownMutationCount() {
        Permanent hemophage = addCreatureReady(player1, new InsatiableHemophage());
        mutate(hemophage);
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, hemophage, List.of(hemophage.getCard()), player1.getId()));
        harness.inMutationScope(() -> {
            gd.playerBattlefields.get(player1.getId()).remove(hemophage);
            gd.playerGraveyards.get(player1.getId()).add(hemophage.getCard());
        });

        resolveAllTriggers();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    private void mutate(Permanent hemophage) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, hemophage, List.of(hemophage.getCard()), player1.getId()));
        resolveAllTriggers();
    }
}
