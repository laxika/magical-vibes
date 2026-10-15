package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.t.TravelingPhilosopher;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ErebossEmissary.class, TravelingPhilosopher.class, Mountain.class})
class ErebossEmissaryTest extends BaseCardTest {

    @Test
    void castsNormallyWithoutATarget() {
        harness.setHand(player1, List.of(new ErebossEmissary()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent emissary = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, emissary)).isTrue();
        assertThat(emissary.isAttached()).isFalse();
    }

    @Test
    void repeatedActivationsStackAndExpire() {
        Permanent emissary = harness.addToBattlefieldAndReturn(player1, new ErebossEmissary());
        harness.setHand(player1, List.of(new TravelingPhilosopher(), new TravelingPhilosopher()));
        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.handleCardChosen(player1, 0);
            harness.passBothPriorities();
        }

        assertThat(gqs.getEffectivePower(gd, emissary)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, emissary)).isEqualTo(7);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passUntil(TurnStep.CLEANUP);
        assertThat(gqs.getEffectivePower(gd, emissary)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, emissary)).isEqualTo(3);
    }

    @Test
    void bestowResolvesAsCreatureWhenHostLeavesBeforeResolution() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new ErebossEmissary()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, host));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Erebos's Emissary");
        Permanent emissary = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, emissary)).isTrue();
        assertThat(emissary.isAttached()).isFalse();
    }

    @Test
    void pendingPumpBoostsEmissaryWhenHostLeaves() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new ErebossEmissary(), new TravelingPhilosopher()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();
        Permanent emissary = gd.playerBattlefields.get(player1.getId()).getLast();
        harness.activateAbility(player1, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, host));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, emissary)).isTrue();
        assertThat(emissary.isAttached()).isFalse();
        assertThat(gqs.getEffectivePower(gd, emissary)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, emissary)).isEqualTo(5);
    }

    @Test
    void pendingPumpStillBoostsOpponentsHostWhenAuraLeaves() {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new ErebossEmissary(), new TravelingPhilosopher()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();
        Permanent emissary = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, emissary));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(4);
        harness.assertInGraveyard(player1, "Erebos's Emissary");
        harness.assertInGraveyard(player1, "Traveling Philosopher");
    }

    @Test
    @DisplayName("Discarding a creature card gives the creature +2/+2")
    void creatureAbilityBoostsSelf() {
        Permanent emissary = harness.addToBattlefieldAndReturn(player1, new ErebossEmissary());
        harness.setHand(player1, List.of(new TravelingPhilosopher()));

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, emissary)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, emissary)).isEqualTo(5);
        harness.assertInGraveyard(player1, "Traveling Philosopher");
    }

    @Test
    @DisplayName("The ability only allows a creature card to be discarded")
    void abilityRequiresCreatureCard() {
        harness.addToBattlefield(player1, new ErebossEmissary());
        harness.setHand(player1, List.of(new Mountain()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a creature card");
    }

    @Test
    @DisplayName("A bestowed Emissary boosts its enchanted creature with both abilities")
    void bestowedAbilityBoostsEnchantedCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new ErebossEmissary(), new TravelingPhilosopher()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(5);

        harness.activateAbility(player1, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(7);
    }

    @Test
    @DisplayName("The temporary boost wears off at end of turn while the Aura boost remains")
    void bestowedTemporaryBoostWearsOff() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new ErebossEmissary(), new TravelingPhilosopher()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(5);
    }
}
