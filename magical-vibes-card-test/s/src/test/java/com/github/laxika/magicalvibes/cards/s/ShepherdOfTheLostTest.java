package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.k.KorSkyfisher;
import com.github.laxika.magicalvibes.cards.k.KrakenHatchling;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShepherdOfTheLost.class, KorSkyfisher.class, KrakenHatchling.class})
class ShepherdOfTheLostTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a ground creature from blocking")
    void flyingPreventsGroundCreatureFromBlocking() {
        addCreatureReady(player1, new ShepherdOfTheLost());
        addCreatureReady(player2, new KrakenHatchling());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("First strike kills a flying blocker before it can deal damage")
    void firstStrikeKillsBlockerBeforeItDealsDamage() {
        addCreatureReady(player1, new ShepherdOfTheLost());
        addCreatureReady(player2, new KorSkyfisher());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shepherd of the Lost");
        harness.assertInGraveyard(player2, "Kor Skyfisher");
        assertThat(findPermanent(player1, "Shepherd of the Lost").getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Vigilance keeps Shepherd of the Lost untapped after attacking")
    void vigilanceKeepsItUntappedWhenAttacking() {
        Permanent shepherd = addCreatureReady(player1, new ShepherdOfTheLost());

        declareAttackers(List.of(0));

        assertThat(shepherd.isTapped()).isFalse();
    }

    @Test
    @DisplayName("First strike also kills an attacker before it deals damage")
    void firstStrikeWorksWhenBlocking() {
        addCreatureReady(player1, new KorSkyfisher());
        addCreatureReady(player2, new ShepherdOfTheLost());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Kor Skyfisher");
        harness.assertOnBattlefield(player2, "Shepherd of the Lost");
        assertThat(findPermanent(player2, "Shepherd of the Lost").getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An unblocked Shepherd deals damage only once")
    void firstStrikeDoesNotDealDamageAgainInRegularDamageStep() {
        addCreatureReady(player1, new ShepherdOfTheLost());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 17);
    }
}
