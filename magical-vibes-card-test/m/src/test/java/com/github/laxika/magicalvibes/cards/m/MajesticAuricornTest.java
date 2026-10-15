package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MajesticAuricorn.class})
class MajesticAuricornTest extends BaseCardTest {

    @Test
    @DisplayName("Mutating gains 4 life")
    void mutatingGainsFourLife() {
        Permanent auricorn = addCreatureReady(player1, new MajesticAuricorn());
        int lifeBefore = gd.getLife(player1.getId());

        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, auricorn, List.of(auricorn.getCard()), player1.getId()));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
    }

    @Test
    @DisplayName("Casting normally does not gain life")
    void castingNormallyDoesNotGainLife() {
        int lifeBefore = gd.getLife(player1.getId());

        harness.castFromHand(player1, new MajesticAuricorn(), "{4}{W}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Majestic Auricorn");
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("The mutation trigger gains life only when it resolves and only for its controller")
    void mutationTriggerWaitsForResolutionAndBenefitsItsController() {
        Permanent auricorn = addCreatureReady(player2, new MajesticAuricorn());
        int firstLife = gd.getLife(player1.getId());
        int secondLife = gd.getLife(player2.getId());

        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, auricorn, List.of(auricorn.getCard()), player2.getId()));

        harness.assertLife(player1, firstLife);
        harness.assertLife(player2, secondLife);
        resolveAllTriggers();

        harness.assertLife(player1, firstLife);
        harness.assertLife(player2, secondLife + 4);
    }

    @Test
    @DisplayName("Every mutation gains exactly four life")
    void repeatedMutationsEachGainFourLife() {
        Permanent auricorn = addCreatureReady(player1, new MajesticAuricorn());
        int lifeBefore = gd.getLife(player1.getId());

        for (int mutation = 1; mutation <= 3; mutation++) {
            harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                    gd, auricorn, List.of(auricorn.getCard()), player1.getId()));
            resolveAllTriggers();

            harness.assertLife(player1, lifeBefore + 4 * mutation);
        }
    }

    @Test
    @DisplayName("Removing the source does not stop its pending life gain")
    void mutationTriggerResolvesAfterSourceLeavesBattlefield() {
        Permanent auricorn = addCreatureReady(player1, new MajesticAuricorn());
        int lifeBefore = gd.getLife(player1.getId());

        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, auricorn, List.of(auricorn.getCard()), player1.getId()));
        harness.inMutationScope(() -> {
            gd.playerBattlefields.get(player1.getId()).remove(auricorn);
            gd.playerGraveyards.get(player1.getId()).add(auricorn.getCard());
        });
        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore + 4);
    }
}
