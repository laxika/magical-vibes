package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnduringVictory.class, GiantSpider.class, GrizzlyBears.class})
class EnduringVictoryTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target attacking creature and bolsters the least-tough creature")
    void destroysAttackerAndBolsters() {
        Permanent attacker = addAttacker(player2);
        Permanent leastToughCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent largerCreature = addCreatureReady(player1, new GiantSpider());

        cast(attacker.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(leastToughCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(largerCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Destroys a target blocking creature")
    void destroysBlocker() {
        Permanent blocker = addBlocker(player2);

        cast(blocker.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new EnduringVictory()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(UUID targetId) {
        harness.setHand(player1, List.of(new EnduringVictory()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private Permanent addAttacker(Player owner) {
        Permanent attacker = addCreatureReady(owner, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }

    private Permanent addBlocker(Player owner) {
        Permanent blocker = addCreatureReady(owner, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(UUID.randomUUID());
        return blocker;
    }
}
