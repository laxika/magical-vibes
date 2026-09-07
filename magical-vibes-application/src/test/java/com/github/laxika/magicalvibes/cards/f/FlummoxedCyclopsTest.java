package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlummoxedCyclops.class, GrizzlyBears.class})
class FlummoxedCyclopsTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot block after two opponent creatures attack")
    void cannotBlockAfterTwoOpponentCreaturesAttack() {
        Permanent cyclops = addCreatureReady(player1, new FlummoxedCyclops());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(cyclops.isCantBlockThisCombat()).isTrue();
    }

    @Test
    @DisplayName("Can block when fewer than two opponent creatures attack")
    void canBlockWhenFewerThanTwoOpponentCreaturesAttack() {
        Permanent cyclops = addCreatureReady(player1, new FlummoxedCyclops());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(cyclops.isCantBlockThisCombat()).isFalse();
    }

    @Test
    @DisplayName("Restriction expires at the end of combat")
    void restrictionExpiresAtEndOfCombat() {
        Permanent cyclops = addCreatureReady(player1, new FlummoxedCyclops());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0, 1));
        harness.passBothPriorities();
        resolveCombat(player2);

        assertThat(cyclops.isCantBlockThisCombat()).isFalse();
    }
}
