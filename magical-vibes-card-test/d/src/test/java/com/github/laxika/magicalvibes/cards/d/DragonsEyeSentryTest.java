package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.k.KolaghanStormsinger;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonsEyeSentry.class, KolaghanStormsinger.class, DragonScarredBear.class})
class DragonsEyeSentryTest extends BaseCardTest {

    @Test
    void cannotAttackBecauseItHasDefender() {
        addCreatureReady(player1, new DragonsEyeSentry());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void firstStrikeKillsAttackerBeforeItDealsDamage() {
        addCreatureReady(player1, new KolaghanStormsinger());
        Permanent sentry = addCreatureReady(player2, new DragonsEyeSentry());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player2, "Dragon's Eye Sentry");
        harness.assertInGraveyard(player1, "Kolaghan Stormsinger");
        assertThat(sentry.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    void firstStrikeDoesNotDealDamageAgainInRegularDamageStep() {
        addCreatureReady(player1, new DragonScarredBear());
        addCreatureReady(player2, new DragonsEyeSentry());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Dragon-Scarred Bear");
        harness.assertInGraveyard(player2, "Dragon's Eye Sentry");
        harness.assertLife(player2, 20);
    }
}
