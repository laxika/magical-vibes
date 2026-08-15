package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DarkSalvationTest extends BaseCardTest {

    @Test
    @DisplayName("Target player gets X Zombies and the creature gets -1/-1 for each Zombie they control")
    void createsZombiesAndScalesReductionFromTargetPlayer() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        harness.addToBattlefield(player2, new ScatheZombies());
        harness.setHand(player1, List.of(new DarkSalvation()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 2, List.of(player2.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("May target only the player to create Zombies")
    void mayOmitCreatureTarget() {
        harness.addToBattlefield(player2, new ScatheZombies());
        harness.setHand(player1, List.of(new DarkSalvation()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 2, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(2);
    }

    @Test
    @DisplayName("X=0 still uses Zombies already controlled by the target player")
    void zeroXStillUsesExistingZombies() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        harness.addToBattlefield(player2, new ScatheZombies());
        harness.setHand(player1, List.of(new DarkSalvation()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 0, List.of(player2.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Creating Zombies still happens when the creature target is gone")
    void createsZombiesWhenCreatureTargetBecomesIllegal() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        harness.addToBattlefield(player2, new ScatheZombies());
        harness.setHand(player1, List.of(new DarkSalvation()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 2, List.of(player2.getId(), creature.getId()));
        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> permanent.getId().equals(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(2);
    }
}
