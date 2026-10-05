package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QueueOfBeetles.class, LightningBolt.class, Shock.class, ShivanDragon.class})
class QueueOfBeetlesTest extends BaseCardTest {

    @Test
    @DisplayName("Makes the first spell on the stack resolve first")
    void resolvesStackFirstInFirstOut() {
        harness.addToBattlefield(player1, new QueueOfBeetles());
        harness.setHand(player1, List.of(new LightningBolt(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("An opponent's Queue of Beetles also changes spell resolution order")
    void affectsBothPlayers() {
        harness.addToBattlefield(player2, new QueueOfBeetles());
        harness.setHand(player1, List.of(new LightningBolt(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 15);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing Queue of Beetles restores last in, first out for remaining spells")
    void restoresNormalOrderAfterLeavingBattlefield() {
        Permanent beetles = harness.addToBattlefieldAndReturn(player1, new QueueOfBeetles());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, beetles.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Queue of Beetles");
        harness.assertInGraveyard(player1, "Queue of Beetles");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 15);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Queue of Beetles does not change resolution order while it is a creature spell")
    void hasNoEffectWhileOnStack() {
        harness.setHand(player1, List.of(new QueueOfBeetles(), new LightningBolt(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertLife(player2, 15);
        harness.assertNotOnBattlefield(player1, "Queue of Beetles");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Queue of Beetles");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activated abilities resolve in first in, first out order alongside spells")
    void resolvesActivatedAbilityBeforeLaterSpell() {
        harness.addToBattlefield(player1, new QueueOfBeetles());
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new ShivanDragon());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(6);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
        assertThat(gd.stack).isEmpty();
    }
}
