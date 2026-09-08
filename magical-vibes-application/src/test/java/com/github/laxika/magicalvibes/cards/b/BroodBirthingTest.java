package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SkitteringInvasion;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.setHand(player1, List.of(new SkitteringInvasion()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, 0);
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

    private void castBroodBirthing() {
        harness.setHand(player1, List.of(new BroodBirthing()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
