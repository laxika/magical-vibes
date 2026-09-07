package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BogWraith;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpectralForce.class, BogWraith.class, GrizzlyBears.class})
class SpectralForceTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking into a defender with no black permanents locks Spectral Force's next untap")
    void locksUntapWhenDefenderHasNoBlackPermanents() {
        Permanent force = addCreatureReady(player1, new SpectralForce());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(force.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("No untap lock when the defending player controls a black permanent")
    void noLockWhenDefenderHasBlackPermanent() {
        Permanent force = addCreatureReady(player1, new SpectralForce());
        addCreatureReady(player2, new BogWraith());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(force.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Only the defending player's permanents matter")
    void controllersBlackPermanentIsIgnored() {
        Permanent force = addCreatureReady(player1, new SpectralForce());
        addCreatureReady(player1, new BogWraith());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(force.getSkipUntapCount()).isEqualTo(1);
    }
}
