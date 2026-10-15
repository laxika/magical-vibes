package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.y.YamabushisFlame;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KitsuneHealer.class, KokushoTheEveningStar.class, KitsuneBlademaster.class,
        YamabushisFlame.class})
class KitsuneHealerTest extends BaseCardTest {

    private Permanent addHealerReady() {
        return addCreatureReady(player1, new KitsuneHealer());
    }

    @Test
    @DisplayName("Prevents the next 1 damage to any target")
    void preventsNextDamageToAnyTarget() {
        addHealerReady();

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents only the next 1 damage to a target player")
    void preventsOnlyNextDamageToPlayer() {
        addHealerReady();
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        castYamabushisFlameAt(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerDamagePreventionShields).doesNotContainKey(player2.getId());
    }

    @Test
    @DisplayName("Prevents only the next 1 damage to a target creature")
    void preventsOnlyNextDamageToCreature() {
        addHealerReady();
        Permanent target = addCreatureReady(player2, new KokushoTheEveningStar());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        castYamabushisFlameAt(target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(target.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("Prevents all damage to a target legendary creature")
    void preventsAllDamageToLegendaryCreature() {
        addHealerReady();
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new KokushoTheEveningStar()).getId();
        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        assertThat(gd.creaturesWithAllDamagePrevented).contains(targetId);
    }

    @Test
    @DisplayName("Prevents noncombat damage to a target legendary creature")
    void preventsNoncombatDamageToLegendaryCreature() {
        addHealerReady();
        Permanent target = addCreatureReady(player2, new KokushoTheEveningStar());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        castYamabushisFlameAt(target.getId());

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Rejects a nonlegendary creature for the second ability")
    void rejectsNonlegendaryCreature() {
        addHealerReady();
        Permanent target = addCreatureReady(player2, new KitsuneBlademaster());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a player for the second ability")
    void rejectsPlayer() {
        addHealerReady();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castYamabushisFlameAt(UUID targetId) {
        harness.setHand(player1, List.of(new YamabushisFlame()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    @Test
    @DisplayName("The one-damage shield is consumed and does not prevent a later damage event")
    void oneDamageShieldIsConsumed() {
        addHealerReady();
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        castYamabushisFlameAt(player2.getId());
        castYamabushisFlameAt(player2.getId());

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("The legendary creature shield prevents multiple damage events")
    void legendaryShieldPreventsRepeatedDamage() {
        addHealerReady();
        Permanent target = addCreatureReady(player2, new KokushoTheEveningStar());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        castYamabushisFlameAt(target.getId());
        castYamabushisFlameAt(target.getId());

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Both abilities require tapping and cannot be activated again while tapped")
    void abilitiesRequireTapping(int abilityIndex) {
        Permanent healer = addHealerReady();
        Permanent target = addCreatureReady(player2, new KokushoTheEveningStar());

        harness.activateAbility(player1, 0, abilityIndex, null, target.getId());
        harness.passBothPriorities();

        assertThat(healer.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("A summoning-sick healer cannot activate either tap ability")
    void summoningSicknessPreventsActivation(int abilityIndex) {
        harness.addToBattlefield(player1, new KitsuneHealer());
        Permanent target = addCreatureReady(player2, new KokushoTheEveningStar());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Both creature shields expire at end of turn")
    void creatureShieldsExpireAtEndOfTurn(int abilityIndex) {
        addHealerReady();
        Permanent target = addCreatureReady(player2, new KokushoTheEveningStar());

        harness.activateAbility(player1, 0, abilityIndex, null, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        castYamabushisFlameAt(target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Both abilities resolve even if the healer is removed in response")
    void abilitySurvivesSourceRemoval(int abilityIndex) {
        Permanent healer = addHealerReady();
        Permanent target = addCreatureReady(player2, new KokushoTheEveningStar());

        harness.activateAbility(player1, 0, abilityIndex, null, target.getId());
        castYamabushisFlameAt(healer.getId());
        harness.passBothPriorities();
        castYamabushisFlameAt(target.getId());

        harness.assertNotOnBattlefield(player1, "Kitsune Healer");
        assertThat(target.getMarkedDamage()).isEqualTo(abilityIndex == 0 ? 2 : 0);
    }

    @Test
    @DisplayName("An unused player shield expires at end of turn")
    void playerShieldExpiresAtEndOfTurn() {
        addHealerReady();
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        castYamabushisFlameAt(player2.getId());

        harness.assertLife(player2, 17);
    }
}
