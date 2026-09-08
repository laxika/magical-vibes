package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinShrineTest extends BaseCardTest {

    @Test
    @DisplayName("Basic Mountain lets Goblin Shrine boost Goblin creatures globally")
    void basicMountainBoostsGoblins() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent ownGoblin = addCreatureReady(player1, new GoblinRaider());
        Permanent opponentGoblin = addCreatureReady(player2, new GoblinRaider());
        Permanent nonGoblin = addCreatureReady(player1, new GrizzlyBears());
        Permanent shrine = harness.addToBattlefieldAndReturn(player1, new GoblinShrine());
        shrine.setAttachedTo(mountain.getId());
        assertThat(gqs.getEffectivePower(gd, ownGoblin)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentGoblin)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, nonGoblin)).isEqualTo(2);
    }

    @Test
    @DisplayName("Goblin Shrine does not boost Goblins when attached to a non-Mountain land")
    void nonMountainDoesNotBoostGoblins() {
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent goblin = addCreatureReady(player1, new GoblinRaider());
        Permanent shrine = harness.addToBattlefieldAndReturn(player1, new GoblinShrine());
        shrine.setAttachedTo(plains.getId());

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
    }

    @Test
    @DisplayName("When Goblin Shrine leaves, it deals 1 damage to each Goblin creature")
    void damagesEachGoblinWhenItLeaves() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent ownGoblin = addCreatureReady(player1, new GoblinRaider());
        Permanent opponentGoblin = addCreatureReady(player2, new GoblinRaider());
        Permanent nonGoblin = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new GoblinShrine(), new Disenchant()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, mountain.getId());
        harness.passBothPriorities();

        Permanent shrine = findPermanent(player1, "Goblin Shrine");
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, shrine.getId());
        for (int i = 0; i < 4 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }

        assertThat(ownGoblin.getMarkedDamage()).isEqualTo(1);
        assertThat(opponentGoblin.getMarkedDamage()).isEqualTo(1);
        assertThat(nonGoblin.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Goblin Shrine can target lands but not nonland permanents")
    void targetsOnlyLands() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new GoblinShrine()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
