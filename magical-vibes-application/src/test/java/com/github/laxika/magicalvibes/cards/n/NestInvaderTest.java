package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NestInvaderTest extends BaseCardTest {

    @Test
    @DisplayName("When Nest Invader enters, it creates one Eldrazi Spawn token")
    void enteringCreatesOneSpawnToken() {
        castNestInvader();

        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(1);
    }

    @Test
    @DisplayName("The Eldrazi Spawn can be sacrificed to add colorless mana")
    void spawnSacrificeAddsColorlessMana() {
        castNestInvader();

        Permanent spawn = findPermanent(player1, "Eldrazi Spawn");
        int spawnIndex = gd.playerBattlefields.get(player1.getId()).indexOf(spawn);

        harness.activateAbility(player1, spawnIndex, null, null);

        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    private void castNestInvader() {
        harness.setHand(player1, List.of(new NestInvader()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
