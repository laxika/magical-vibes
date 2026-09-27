package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.a.AstralSlide;
import com.github.laxika.magicalvibes.cards.c.CreditVoucher;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Aberrant.class, AstralSlide.class, CreditVoucher.class, GrizzlyBears.class})
class AberrantTest extends BaseCardTest {

    @Test
    @DisplayName("Ravenous enters with X +1/+1 counters without drawing below X=5")
    void ravenousBelowThreshold() {
        castAberrant(3);

        assertThat(findPermanent(player1, "Aberrant").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Ravenous draws a card when X is 5 or more")
    void ravenousDrawsAtThreshold() {
        castAberrant(5);

        assertThat(findPermanent(player1, "Aberrant").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Combat damage destroys a target artifact or enchantment controlled by the damaged player")
    void destroysDamagedPlayersArtifactOrEnchantment() {
        Permanent aberrant = addCreatureReady(player1, new Aberrant());
        aberrant.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        aberrant.setAttacking(true);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CreditVoucher());

        resolveCombat();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Credit Voucher");
        harness.assertInGraveyard(player2, "Credit Voucher");
    }

    @Test
    @DisplayName("The combat-damage trigger only offers artifacts and enchantments controlled by the damaged player")
    void onlyDamagedPlayersArtifactsAndEnchantmentsAreLegal() {
        Permanent aberrant = addCreatureReady(player1, new Aberrant());
        aberrant.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        aberrant.setAttacking(true);
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new CreditVoucher());
        Permanent enemyArtifact = harness.addToBattlefieldAndReturn(player2, new CreditVoucher());
        Permanent enemyEnchantment = harness.addToBattlefieldAndReturn(player2, new AstralSlide());
        Permanent enemyCreature = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(enemyArtifact.getId(), enemyEnchantment.getId())
                .doesNotContain(ownArtifact.getId(), enemyCreature.getId());
    }

    @Test
    @DisplayName("No combat-damage trigger is offered when the damaged player controls no artifact or enchantment")
    void noTriggerWithoutValidTarget() {
        Permanent aberrant = addCreatureReady(player1, new Aberrant());
        aberrant.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        aberrant.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castAberrant(int x) {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Aberrant()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, x + 1);

        gs.playCard(gd, player1, 0, x, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
