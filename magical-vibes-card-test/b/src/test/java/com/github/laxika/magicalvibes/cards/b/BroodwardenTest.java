package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DreadDrone;
import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Broodwarden.class, DreadDrone.class, GrizzlyBears.class, AmoeboidChangeling.class})
class BroodwardenTest extends BaseCardTest {

    @Test
    @DisplayName("Eldrazi Spawn creatures you control get +2/+1")
    void buffsOwnEldraziSpawnCreatures() {
        harness.addToBattlefield(player1, new Broodwarden());
        castDreadDrone();

        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(2);
        for (Permanent spawn : findPermanents(player1, "Eldrazi Spawn")) {
            assertThat(gqs.getEffectivePower(gd, spawn)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, spawn)).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("Broodwarden does not buff other creatures")
    void doesNotBuffOtherCreatures() {
        harness.addToBattlefield(player1, new Broodwarden());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing Broodwarden removes its bonus")
    void bonusIsRemovedWhenBroodwardenLeaves() {
        harness.addToBattlefield(player1, new Broodwarden());
        castDreadDrone();

        Permanent spawn = findPermanent(player1, "Eldrazi Spawn");
        assertThat(gqs.getEffectivePower(gd, spawn)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spawn)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Broodwarden"));

        assertThat(gqs.getEffectivePower(gd, spawn)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, spawn)).isEqualTo(1);
    }

    private void castDreadDrone() {
        harness.castFromHand(player1, new DreadDrone(), "{4}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Opponent's Eldrazi Spawn do not get the bonus")
    void doesNotBuffOpponentsSpawn() {
        harness.addToBattlefield(player1, new Broodwarden());
        harness.enterBattlefieldAndReturn(player2, new DreadDrone());
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Eldrazi Spawn")).hasSize(2);
        for (Permanent spawn : findPermanents(player2, "Eldrazi Spawn")) {
            assertThat(gqs.getEffectivePower(gd, spawn)).isZero();
            assertThat(gqs.getEffectiveToughness(gd, spawn)).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("Multiple Broodwardens give cumulative bonuses")
    void multipleBroodwardensStack() {
        harness.addToBattlefield(player1, new Broodwarden());
        harness.addToBattlefield(player1, new Broodwarden());
        castDreadDrone();

        Permanent spawn = findPermanent(player1, "Eldrazi Spawn");
        assertThat(gqs.getEffectivePower(gd, spawn)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, spawn)).isEqualTo(3);
    }

    @Test
    @DisplayName("Broodwarden immediately buffs Spawn that were already on the battlefield")
    void buffsExistingSpawnOnEntry() {
        castDreadDrone();
        Permanent spawn = findPermanent(player1, "Eldrazi Spawn");
        assertThat(gqs.getEffectivePower(gd, spawn)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, spawn)).isEqualTo(1);

        harness.castFromHand(player1, new Broodwarden(), "{3}{G}{G}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, spawn)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spawn)).isEqualTo(2);
    }

    @Test
    @DisplayName("Eldrazi creatures without the Spawn type do not get the bonus")
    void doesNotBuffEldraziDrone() {
        harness.addToBattlefield(player1, new Broodwarden());
        Permanent drone = harness.addToBattlefieldAndReturn(player1, new DreadDrone());

        assertThat(gqs.getEffectivePower(gd, drone)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, drone)).isEqualTo(1);
    }

    @Test
    @DisplayName("Changeling creatures have both required types and receive the bonus")
    void buffsChangelingCreature() {
        harness.addToBattlefield(player1, new Broodwarden());
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new AmoeboidChangeling());

        assertThat(gqs.getEffectivePower(gd, changeling)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, changeling)).isEqualTo(2);
    }

    @Test
    @DisplayName("Broodwarden receives its own bonus when it gains all creature types")
    void buffsItselfWhenItBecomesEldraziSpawn() {
        addCreatureReady(player1, new AmoeboidChangeling());
        Permanent broodwarden = harness.addToBattlefieldAndReturn(player1, new Broodwarden());

        harness.activateAbility(player1, 0, 0, null, broodwarden.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, broodwarden)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, broodwarden)).isEqualTo(5);
    }

    @Test
    @DisplayName("Losing creature types removes the Spawn bonus")
    void spawnLosingCreatureTypesLosesBonus() {
        addCreatureReady(player1, new AmoeboidChangeling());
        harness.addToBattlefield(player1, new Broodwarden());
        castDreadDrone();
        Permanent spawn = findPermanent(player1, "Eldrazi Spawn");
        assertThat(gqs.getEffectivePower(gd, spawn)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spawn)).isEqualTo(2);

        harness.activateAbility(player1, 0, 1, null, spawn.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, spawn)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, spawn)).isEqualTo(1);
    }
}
