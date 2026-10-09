package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GuardianShieldBearer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonScarredBear.class, GuardianShieldBearer.class})
class DragonScarredBearTest extends BaseCardTest {

    @Test
    @DisplayName("Formidable regeneration ability grants a regeneration shield")
    void formidableRegenerationGrantsShield() {
        Permanent bear = addCreatureReady(player1, new DragonScarredBear());
        addSupportCreatures(player1, 3);
        addRegenerationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bear.getRegenerationShield()).isEqualTo(1);
        assertThat(bear.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Regeneration ability can be activated at exactly eight total power")
    void canActivateAtExactThreshold() {
        Permanent bear = addCreatureReady(player1, new DragonScarredBear());
        addSupportCreatures(player1, 3);
        bear.setPowerModifier(-1);
        addRegenerationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bear.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration ability cannot be activated below eight total power")
    void cannotActivateBelowThreshold() {
        Permanent bear = addCreatureReady(player1, new DragonScarredBear());
        addSupportCreatures(player1, 2);
        addRegenerationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total power 8 or greater");
        assertThat(bear.getRegenerationShield()).isZero();
    }

    @Test
    void resolvesEvenIfTotalPowerFallsBelowEightAfterActivation() {
        Permanent bear = addCreatureReady(player1, new DragonScarredBear());
        addSupportCreatures(player1, 3);
        addRegenerationMana();

        harness.activateAbility(player1, 0, null, null);
        bear.setPowerModifier(-2);
        harness.passBothPriorities();

        assertThat(bear.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void opponentsCreaturesDoNotCountTowardFormidable() {
        Permanent bear = addCreatureReady(player1, new DragonScarredBear());
        addSupportCreatures(player1, 2);
        addSupportCreatures(player2, 3);
        addRegenerationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total power 8 or greater");
        assertThat(bear.getRegenerationShield()).isZero();
    }

    @Test
    void negativePowerCountsInTheTotal() {
        Permanent bear = addCreatureReady(player1, new DragonScarredBear());
        addSupportCreatures(player1, 3);
        Permanent weakenedCreature = addCreatureReady(player1, new GuardianShieldBearer());
        weakenedCreature.setPowerModifier(-4);
        addRegenerationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total power 8 or greater");
        assertThat(bear.getRegenerationShield()).isZero();
    }

    @Test
    void tappedSummoningSickBearCanActivateRegeneration() {
        Permanent bear = addCreatureReady(player1, new DragonScarredBear());
        bear.setSummoningSick(true);
        bear.tap();
        addSupportCreatures(player1, 3);
        addRegenerationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bear.getRegenerationShield()).isEqualTo(1);
        assertThat(bear.isTapped()).isTrue();
    }

    @Test
    void repeatedActivationsProtectAgainstSeparateDestructionEvents() {
        Permanent bear = addCreatureReady(player1, new DragonScarredBear());
        addSupportCreatures(player1, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bear.getRegenerationShield()).isEqualTo(2);
        bear.setMarkedDamage(1);
        harness.inMutationScope(() ->
                assertThat(harness.getPermanentRemovalService().tryDestroyPermanent(gd, bear)).isFalse());
        assertThat(bear.getRegenerationShield()).isEqualTo(1);
        assertThat(bear.getMarkedDamage()).isZero();
        assertThat(bear.isTapped()).isTrue();
        harness.inMutationScope(() ->
                assertThat(harness.getPermanentRemovalService().tryDestroyPermanent(gd, bear)).isFalse());
        assertThat(bear.getRegenerationShield()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
        harness.inMutationScope(() ->
                assertThat(harness.getPermanentRemovalService().tryDestroyPermanent(gd, bear)).isTrue());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);
    }

    private void addSupportCreatures(Player player, int count) {
        for (int i = 0; i < count; i++) {
            addCreatureReady(player, new GuardianShieldBearer());
        }
    }

    private void addRegenerationMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
