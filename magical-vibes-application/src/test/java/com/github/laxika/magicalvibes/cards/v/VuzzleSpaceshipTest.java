package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VuzzleSpaceship.class, GrizzlyBears.class, Shock.class})
class VuzzleSpaceshipTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with six +1/+1 counters and has flying")
    void entersWithCountersAndFlying() {
        Permanent spaceship = castSpaceship();

        assertThat(spaceship.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, spaceship, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Damage removes counters instead and disables threshold abilities")
    void damageRemovesCountersAndDisablesFlying() {
        Permanent spaceship = addSpaceship(player2);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, spaceship.getId());
        harness.passBothPriorities();

        assertThat(spaceship.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(spaceship.getMarkedDamage()).isZero();
        assertThat(gqs.hasKeyword(gd, spaceship, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Lasers deals one damage on attack while it has at least three counters")
    void lasersDealsDamageOnAttack() {
        Permanent spaceship = addSpaceship(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(spaceship.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    private Permanent addSpaceship(com.github.laxika.magicalvibes.model.Player player) {
        Permanent spaceship = addCreatureReady(player, new VuzzleSpaceship());
        spaceship.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 6);
        return spaceship;
    }

    private Permanent castSpaceship() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new VuzzleSpaceship()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Vuzzle Spaceship");
    }
}
