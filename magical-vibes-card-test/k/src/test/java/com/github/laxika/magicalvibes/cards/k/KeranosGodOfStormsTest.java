package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.CephalidColiseum;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KeranosGodOfStorms.class, CephalidColiseum.class, FugitiveWizard.class,
        GrizzlyBears.class, RagingGoblin.class})
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

    @Test
    @DisplayName("Keranos stops being a creature when devotion drops from seven to six")
    void stopsBeingCreatureWhenDevotionDrops() {
        Permanent keranos = addKeranos();
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new RagingGoblin());
        }
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        assertThat(gqs.isCreature(gd, keranos)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(wizard);

        assertThat(gqs.isCreature(gd, keranos)).isFalse();
        assertThat(gqs.isEnchantment(gd, keranos)).isTrue();
    }

    @Test
    @DisplayName("Keranos can deal damage to a creature while it is not a creature itself")
    void noncreatureKeranosDealsDamageToCreature() {
        addKeranos();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @CardUsed({TurnToFrog.class})
    @DisplayName("Keranos does not trigger after losing all abilities")
    void doesNotTriggerAfterLosingAbilities() {
        Permanent keranos = addKeranos();
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new RagingGoblin());
        }
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, keranos.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasLostAllAbilities(gd, keranos)).isTrue();
        harness.setLibrary(player1, List.of(new CephalidColiseum(), new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Entering after the first draw does not make the next draw trigger Keranos")
    void enteringAfterFirstDrawDoesNotTriggerOnSecondDraw() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new CephalidColiseum()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        addKeranos();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("An opponent drawing on Keranos's controller's turn does not trigger Keranos")
    void opponentDrawDoesNotTriggerKeranos() {
        addKeranos();
        harness.setLibrary(player2, List.of(new CephalidColiseum(), new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    private Permanent addKeranos() {
        return harness.addToBattlefieldAndReturn(player1, new KeranosGodOfStorms());
    }
}
