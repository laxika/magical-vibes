package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.ChandraSparkHunter;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MigratingKetradon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OutpaceOblivion.class, MigratingKetradon.class, Forest.class, ChandraSparkHunter.class})
class OutpaceOblivionTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 5 damage to up to one target creature")
    void etbDealsDamageToTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MigratingKetradon());
        castOutpace(target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("ETB can resolve without a target")
    void etbCanResolveWithoutTarget() {
        harness.castFromHand(player1, new OutpaceOblivion(), "{2}{R}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Outpace Oblivion");
    }

    @Test
    @DisplayName("Sacrifice ability damages only players below max speed")
    void sacrificeAbilityDamagesOnlyPlayersBelowMaxSpeed() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new OutpaceOblivion());
        gd.playerSpeeds.put(player1.getId(), 3);
        gd.playerSpeeds.put(player2.getId(), 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int player1Life = gd.getLife(player1.getId());
        int player2Life = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1Life - 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2Life);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source.getCard());
    }

    @Test
    @DisplayName("ETB cannot target a land")
    void etbCannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new OutpaceOblivion()));
        addManaForOutpace();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must");
    }

    @Test
    void etbDestroysPlaneswalkerThroughLoyaltyDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraSparkHunter());
        target.setCounterCount(CounterType.LOYALTY, 4);

        castOutpace(target.getId());

        harness.assertNotOnBattlefield(player2, "Chandra, Spark Hunter");
        harness.assertInGraveyard(player2, "Chandra, Spark Hunter");
    }

    @Test
    void etbCanDamageOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MigratingKetradon());

        castOutpace(target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    void mayChooseNoTargetEvenWhenCreatureIsAvailable() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MigratingKetradon());

        harness.castFromHand(player1, new OutpaceOblivion(), "{2}{R}");
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Outpace Oblivion");
    }

    @Test
    void enteringStartsSpeedAndDoesNotResetExistingSpeed() {
        harness.castFromHand(player1, new OutpaceOblivion(), "{2}{R}");
        resolveAllTriggers();
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);

        gd.playerSpeeds.put(player1.getId(), 3);
        harness.castFromHand(player1, new OutpaceOblivion(), "{2}{R}");
        resolveAllTriggers();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(3);
    }

    @ParameterizedTest
    @CsvSource({"1, 0, 18, 18", "2, 3, 18, 18", "4, 0, 20, 18", "4, 4, 20, 20"})
    void sacrificeDamageIncludesPlayersWithNoSpeedAndExcludesMaxSpeed(
            int controllerSpeed, int opponentSpeed, int controllerLife, int opponentLife) {
        harness.addToBattlefield(player1, new OutpaceOblivion());
        gd.playerSpeeds.put(player1.getId(), controllerSpeed);
        if (opponentSpeed > 0) {
            gd.playerSpeeds.put(player2.getId(), opponentSpeed);
        }
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Outpace Oblivion");
        harness.assertInGraveyard(player1, "Outpace Oblivion");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        resolveAllTriggers();

        harness.assertLife(player1, controllerLife);
        harness.assertLife(player2, opponentLife);
    }

    @Test
    void sacrificeAbilityChecksSpeedOnResolution() {
        harness.addToBattlefield(player1, new OutpaceOblivion());
        gd.playerSpeeds.put(player1.getId(), 4);
        gd.playerSpeeds.put(player2.getId(), 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        gd.playerSpeeds.put(player2.getId(), 4);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void sacrificeDamageIncreasesSpeedOnControllersTurnAfterSourceLeaves() {
        harness.addToBattlefield(player1, new OutpaceOblivion());
        gd.playerSpeeds.put(player1.getId(), 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Outpace Oblivion");
    }

    @Test
    void sacrificeAbilityCanBeActivatedOnOpponentsTurnWithoutIncreasingOwnSpeed() {
        harness.addToBattlefield(player1, new OutpaceOblivion());
        gd.playerSpeeds.put(player1.getId(), 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void etbStillDealsDamageAfterSourceIsSacrificed() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MigratingKetradon());
        harness.setHand(player1, List.of(new OutpaceOblivion()));
        addManaForOutpace();
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(5);
        harness.assertInGraveyard(player1, "Outpace Oblivion");
    }

    @Test
    void etbDoesNotDamageTargetThatLeftBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MigratingKetradon());
        harness.setHand(player1, List.of(new OutpaceOblivion()));
        addManaForOutpace();
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerHands.get(player2.getId()).add(target.getCard());

        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInHand(player2, "Migrating Ketradon");
        harness.assertOnBattlefield(player1, "Outpace Oblivion");
        assertThat(gd.stack).isEmpty();
    }

    private void castOutpace(UUID targetId) {
        harness.setHand(player1, List.of(new OutpaceOblivion()));
        addManaForOutpace();
        harness.castEnchantment(player1, 0, targetId);
        resolveAllTriggers();
    }

    private void addManaForOutpace() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
