package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmrakulsHatcherTest extends BaseCardTest {

    @Test
    @DisplayName("When Emrakul's Hatcher enters, it creates three Eldrazi Spawn tokens")
    void enteringCreatesThreeSpawnTokens() {
        castAndResolve();

        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(3);
    }

    @Test
    @DisplayName("An Eldrazi Spawn can be sacrificed to add colorless mana")
    void spawnSacrificeAddsColorlessMana() {
        castAndResolve();

        Permanent spawn = findPermanent(player1, "Eldrazi Spawn");
        int spawnIndex = gd.playerBattlefields.get(player1.getId()).indexOf(spawn);

        harness.activateAbility(player1, spawnIndex, null, null);

        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new EmrakulsHatcher()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
