package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThassasEmissary.class, TravelingPhilosopher.class})
class ThassasEmissaryTest extends BaseCardTest {

    @Test
    @DisplayName("Thassa's Emissary draws a card when it deals combat damage to a player")
    void drawsOnCombatDamageToPlayerAsCreature() {
        Permanent emissary = addCreatureReady(player1, new ThassasEmissary());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        emissary.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Bestow boosts the enchanted creature and draws when it deals combat damage")
    void bestowBoostsAndGrantsCombatDamageTrigger() {
        Permanent bear = addCreatureReady(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new ThassasEmissary()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(5);

        int handSizeBeforeCombat = gd.playerHands.get(player1.getId()).size();
        bear.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBeforeCombat + 1);
    }

    @Test
    @DisplayName("A bestowed Thassa's Emissary becomes a creature when its host leaves")
    void becomesCreatureWhenHostLeaves() {
        Permanent bear = addCreatureReady(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new ThassasEmissary()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();
        Permanent emissary = findPermanent(player1, "Thassa's Emissary");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(emissary);
        assertThat(gqs.isCreature(gd, emissary)).isTrue();
        assertThat(emissary.isAttached()).isFalse();
    }

    @Test
    @DisplayName("Normal casting needs no target and does not boost another creature")
    void castsNormallyWithoutTarget() {
        Permanent host = addCreatureReady(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new ThassasEmissary()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent emissary = findPermanent(player1, "Thassa's Emissary");
        assertThat(gqs.isCreature(gd, emissary)).isTrue();
        assertThat(emissary.isAttached()).isFalse();
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bestow resolves as a creature when its target leaves before resolution")
    void resolvesAsCreatureWhenTargetLeaves() {
        Permanent host = addCreatureReady(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new ThassasEmissary()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, host));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent emissary = findPermanent(player1, "Thassa's Emissary");
        assertThat(gqs.isCreature(gd, emissary)).isTrue();
        assertThat(emissary.isAttached()).isFalse();
        harness.assertNotInGraveyard(player1, "Thassa's Emissary");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The Aura's controller draws when an enchanted opposing creature hits them")
    void auraControllerDrawsWhenOpposingHostDealsCombatDamage() {
        Permanent host = addCreatureReady(player2, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new ThassasEmissary()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();

        Permanent emissary = findPermanent(player1, "Thassa's Emissary");
        assertThat(emissary.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gqs.isCreature(gd, emissary)).isFalse();
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(5);
        int controllerHandBefore = gd.playerHands.get(player1.getId()).size();
        int hostControllerHandBefore = gd.playerHands.get(player2.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        host.setAttacking(true);
        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 5);
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(controllerHandBefore + 1);
        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(hostControllerHandBefore);
    }

    @Test
    @DisplayName("The normal casting cost cannot pay the bestow cost")
    void cannotBestowForNormalCost() {
        Permanent host = addCreatureReady(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new ThassasEmissary()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, host.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Thassa's Emissary");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Combat damage to a blocking creature does not draw a card")
    void doesNotDrawWhenBlocked() {
        Permanent emissary = addCreatureReady(player1, new ThassasEmissary());
        Permanent blocker = addCreatureReady(player2, new TravelingPhilosopher());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int defendingLifeBefore = gd.playerLifeTotals.get(player2.getId());

        emissary.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(defendingLifeBefore);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("An Emissary that stops being bestowed still draws for its own combat damage")
    void drawsAfterHostLeaves() {
        Permanent host = addCreatureReady(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new ThassasEmissary()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();
        Permanent emissary = findPermanent(player1, "Thassa's Emissary");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, host));
        harness.runStateBasedActions();
        emissary.setSummoningSick(false);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        emissary.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
    }
}
