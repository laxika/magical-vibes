package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToChargeCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GoldenUrnTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Golden Urn has upkeep triggered ability and activated ability")
    void hasCorrectAbilityStructure() {
        GoldenUrn card = new GoldenUrn();

        // Upkeep triggered ability (may put charge counter)
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(PutChargeCounterOnSelfEffect.class);

        // Activated ability (tap + sacrifice to gain life)
        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().get(0);
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeSelfCost.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(GainLifeEqualToChargeCountersOnSourceEffect.class);
    }

    // ===== Upkeep triggered ability =====

    @Test
    @DisplayName("Upkeep trigger may add a charge counter to Golden Urn")
    void upkeepTriggerMayAddChargeCounter() {
        Permanent urn = addReadyUrn(player1);

        harness.forceActivePlayer(player1.getId());
        harness.forceStep("upkeep");

        // Accept the may ability to add a counter
        harness.acceptMayAbility();

        assertThat(urn.getChargeCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Upkeep trigger can decline to add a charge counter")
    void upkeepTriggerCanDeclineToAddCounter() {
        Permanent urn = addReadyUrn(player1);

        harness.forceActivePlayer(player1.getId());
        harness.forceStep("upkeep");

        // Decline the may ability
        harness.declineMayAbility();

        assertThat(urn.getChargeCounters()).isEqualTo(0);
    }

    @Test
    @DisplayName("Multiple upkeep triggers accumulate charge counters")
    void multipleUpkeepTriggersAccumulateCounters() {
        Permanent urn = addReadyUrn(player1);

        // First upkeep - add counter
        harness.forceActivePlayer(player1.getId());
        harness.forceStep("upkeep");
        harness.acceptMayAbility();

        assertThat(urn.getChargeCounters()).isEqualTo(1);

        // Move through turn to next upkeep
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.forceActivePlayer(player1.getId());
        harness.forceStep("upkeep");

        // Second upkeep - add counter
        harness.acceptMayAbility();

        assertThat(urn.getChargeCounters()).isEqualTo(2);
    }

    // ===== Activated ability: Tap and sacrifice to gain life =====

    @Test
    @DisplayName("Sacrificing Golden Urn gains life equal to charge counters")
    void sacrificeSelfGainsLifeEqualToChargeCounters() {
        Permanent urn = addReadyUrn(player1);
        urn.setChargeCounters(3);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Golden Urn should be in graveyard
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Golden Urn"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Golden Urn"));

        // Player should have gained 3 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Sacrificing Golden Urn with 0 counters gains 0 life")
    void sacrificeSelfWithZeroCountersGainsNoLife() {
        Permanent urn = addReadyUrn(player1);
        // No charge counters

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Golden Urn should be in graveyard
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Golden Urn"));

        // Player should not have gained any life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Activated ability requires tap")
    void activatedAbilityRequiresTap() {
        Permanent urn = addReadyUrn(player1);
        urn.tap();

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.activateAbility(player1, 0, null, null)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrificing Golden Urn with accumulated counters gains correct life")
    void sacrificingWithAccumulatedCounters() {
        Permanent urn = addReadyUrn(player1);

        // Add counters through multiple uptaps (manually for testing)
        harness.forceActivePlayer(player1.getId());
        harness.forceStep("upkeep");
        harness.acceptMayAbility();

        assertThat(urn.getChargeCounters()).isEqualTo(1);

        // Untap for activation
        urn.untap();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Should have gained 1 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Golden Urn goes to graveyard immediately on sacrifice")
    void goldenUrnGoesToGraveyardOnSacrifice() {
        Permanent urn = addReadyUrn(player1);
        urn.setChargeCounters(2);

        harness.activateAbility(player1, 0, null, null);

        // Should be in graveyard immediately (sacrifice is a cost)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Golden Urn"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Golden Urn"));
    }

    // ===== Helper methods =====

    private Permanent addReadyUrn(Player player) {
        GoldenUrn card = new GoldenUrn();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
