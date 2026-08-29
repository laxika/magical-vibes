package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.CephalidColiseum;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KeranosGodOfStormsTest extends BaseCardTest {

    @Test
    @DisplayName("Keranos is not a creature below seven combined blue and red devotion")
    void isNotCreatureBelowDevotionThreshold() {
        Permanent keranos = addKeranos();

        assertThat(gqs.isCreature(gd, keranos)).isFalse();
        assertThat(gqs.isEnchantment(gd, keranos)).isTrue();
    }

    @Test
    @DisplayName("Keranos becomes a creature at seven combined blue and red devotion")
    void becomesCreatureAtDevotionThreshold() {
        Permanent keranos = addKeranos();
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new RagingGoblin());
        }
        harness.addToBattlefield(player1, new FugitiveWizard());

        assertThat(gqs.isCreature(gd, keranos)).isTrue();
    }

    @Test
    @DisplayName("The first drawn land causes Keranos to draw an additional card")
    void firstDrawnLandDrawsAdditionalCard() {
        addKeranos();
        harness.setLibrary(player1, List.of(new CephalidColiseum(), new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The first drawn nonland causes Keranos to deal 3 damage to a chosen target")
    void firstDrawnNonlandDealsDamageToTarget() {
        addKeranos();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Only the first draw of the turn is revealed")
    void laterDrawDoesNotTriggerKeranos() {
        addKeranos();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new CephalidColiseum()));
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A draw during an opponent's turn does not trigger Keranos")
    void drawDuringOpponentsTurnDoesNotTriggerKeranos() {
        addKeranos();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player2, 20);
        gd.activePlayerId = player2.getId();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addKeranos() {
        return harness.addToBattlefieldAndReturn(player1, new KeranosGodOfStorms());
    }
}
