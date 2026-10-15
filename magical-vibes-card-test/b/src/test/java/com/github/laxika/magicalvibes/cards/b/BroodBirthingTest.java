package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SkitteringInvasion;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BroodBirthing.class, SkitteringInvasion.class})
class BroodBirthingTest extends BaseCardTest {

    @Test
    @DisplayName("Without an Eldrazi Spawn, Brood Birthing creates one Eldrazi Spawn")
    void createsOneSpawnWithoutExistingSpawn() {
        castBroodBirthing();

        assertThat(findPermanents(player1, "Eldrazi Spawn")).singleElement().satisfies(spawn -> {
            assertThat(spawn.getCard().isToken()).isTrue();
            assertThat(spawn.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(spawn.getCard().getColor()).isNull();
            assertThat(spawn.getCard().getSubtypes())
                    .containsExactlyInAnyOrder(CardSubtype.ELDRAZI, CardSubtype.SPAWN);
            assertThat(gqs.getEffectivePower(gd, spawn)).isZero();
            assertThat(gqs.getEffectiveToughness(gd, spawn)).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("With an Eldrazi Spawn, Brood Birthing creates three Eldrazi Spawn")
    void createsThreeSpawnsWithExistingSpawn() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromHand(player1, new SkitteringInvasion(), "{7}");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new BroodBirthing()));
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(8);
    }

    @Test
    @DisplayName("A Brood Birthing Spawn can be sacrificed for colorless mana")
    void createdSpawnCanBeSacrificedForColorlessMana() {
        castBroodBirthing();

        Permanent spawn = findPermanent(player1, "Eldrazi Spawn");
        int spawnIndex = gd.playerBattlefields.get(player1.getId()).indexOf(spawn);
        harness.activateAbility(player1, spawnIndex, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Eldrazi Spawn does not increase the number created")
    void opponentsSpawnDoesNotSatisfyCondition() {
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new BroodBirthing(), "{1}{R}");
        harness.passBothPriorities();
        harness.forceActivePlayer(player1);

        castBroodBirthing();

        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(1);
        assertThat(findPermanents(player2, "Eldrazi Spawn")).hasSize(1);
    }

    @Test
    @DisplayName("Sacrificing the last Spawn in response selects the one-token branch")
    void checksSpawnConditionAtResolution() {
        castBroodBirthing();
        harness.castFromHand(player1, new BroodBirthing(), "{1}{R}");
        harness.activateAbility(player1, 0, null, null);
        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(1);
    }

    @Test
    @DisplayName("One existing Spawn is sufficient and all three new tokens can produce mana")
    void oneExistingSpawnCreatesThreeWithManaAbilities() {
        castBroodBirthing();
        castBroodBirthing();
        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(4);

        for (int i = 0; i < 4; i++) {
            harness.activateAbility(player1, 0, null, null);
        }

        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
    }

    private void castBroodBirthing() {
        harness.castFromHand(player1, new BroodBirthing(), "{1}{R}");

        harness.passBothPriorities();
    }
}
