package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.a.AstralSlide;
import com.github.laxika.magicalvibes.cards.c.CreditVoucher;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

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

    @Test
    @DisplayName("Ravenous with X=0 dies without drawing a card")
    void ravenousWithZero() {
        castAberrant(0);

        harness.assertNotOnBattlefield(player1, "Aberrant");
        harness.assertInGraveyard(player1, "Aberrant");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Ravenous draws exactly one card above X=5")
    void ravenousAboveThreshold() {
        castAberrant(6);

        assertThat(findPermanent(player1, "Aberrant").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(6);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The enchantment target is chosen before the combat-damage ability resolves")
    void destroysEnchantmentOnResolution() {
        Permanent aberrant = addCreatureReady(player1, new Aberrant());
        aberrant.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        aberrant.setAttacking(true);
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new AstralSlide());

        resolveCombat();
        harness.handlePermanentChosen(player1, enchantment.getId());

        harness.assertOnBattlefield(player2, "Astral Slide");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Astral Slide");
        harness.assertInGraveyard(player2, "Astral Slide");
    }

    @Test
    @DisplayName("The target must still be controlled by the damaged player at resolution")
    void targetChangingControllersIsIllegal() {
        Permanent aberrant = addCreatureReady(player1, new Aberrant());
        aberrant.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        aberrant.setAttacking(true);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CreditVoucher());

        resolveCombat();
        harness.handlePermanentChosen(player1, artifact.getId());
        gd.playerBattlefields.get(player2.getId()).remove(artifact);
        gd.playerBattlefields.get(player1.getId()).add(artifact);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Credit Voucher");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Trample damage to the defending player triggers Heavy Power Hammer")
    void trampleDamageTriggersDestruction() {
        harness.setLife(player2, 20);
        Permanent aberrant = addCreatureReady(player1, new Aberrant());
        aberrant.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CreditVoucher());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 1
        ));
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Credit Voucher");
        harness.assertOnBattlefield(player1, "Aberrant");
    }

    @Test
    @DisplayName("Ravenous does not create a draw trigger when X is below five")
    void noRavenousTriggerBelowThreshold() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Aberrant()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        gs.playCard(gd, player1, 0, 4, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Aberrant");
        assertThat(findPermanent(player1, "Aberrant").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(4);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
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
