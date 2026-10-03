package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreadDrone.class})
class DreadDroneTest extends BaseCardTest {

    @Test
    @DisplayName("When Dread Drone enters, it creates two Eldrazi Spawn tokens")
    void enteringCreatesTwoSpawnTokens() {
        castDreadDrone();

        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(2);
    }

    @Test
    @DisplayName("An Eldrazi Spawn can be sacrificed to add colorless mana")
    void spawnSacrificeAddsColorlessMana() {
        castDreadDrone();

        Permanent spawn = findPermanent(player1, "Eldrazi Spawn");
        int spawnIndex = gd.playerBattlefields.get(player1.getId()).indexOf(spawn);

        harness.activateAbility(player1, spawnIndex, null, null);

        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    private void castDreadDrone() {
        harness.castFromHand(player1, new DreadDrone(), "{4}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Created Spawn are untapped colorless 0/1 Eldrazi Spawn creatures")
    void spawnHaveOracleCharacteristics() {
        castDreadDrone();

        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(2).allSatisfy(spawn -> {
            assertThat(gqs.isCreature(gd, spawn)).isTrue();
            assertThat(gqs.getEffectivePower(gd, spawn)).isZero();
            assertThat(gqs.getEffectiveToughness(gd, spawn)).isEqualTo(1);
            assertThat(gqs.getEffectiveColors(gd, spawn)).isEmpty();
            assertThat(spawn.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ELDRAZI, CardSubtype.SPAWN);
            assertThat(spawn.isTapped()).isFalse();
            assertThat(gqs.isToken(gd, spawn)).isTrue();
        });
    }

    @Test
    @DisplayName("Entering without casting creates Spawn for Dread Drone's controller")
    void enteringWithoutCastingCreatesTokensForController() {
        harness.enterBattlefieldAndReturn(player2, new DreadDrone());
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Eldrazi Spawn")).hasSize(2);
        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();
    }

    @Test
    @DisplayName("Both newly created tapped Spawn can be sacrificed for immediate mana")
    void tappedSpawnCanBothBeSacrificedImmediately() {
        castDreadDrone();

        for (Permanent spawn : findPermanents(player1, "Eldrazi Spawn")) {
            spawn.setTapped(true);
            int index = gd.playerBattlefields.get(player1.getId()).indexOf(spawn);
            harness.activateAbility(player1, index, null, null);
            assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(spawn);
            assertThat(gd.stack).isEmpty();
        }

        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(findPermanents(player1, "Dread Drone")).hasSize(1);
    }
}
