package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.cards.s.SirenStormtamer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrazingWhiptail.class, SirenStormtamer.class, RaptorCompanion.class})
class GrazingWhiptailTest extends BaseCardTest {

    @Test
    void reachAllowsBlockingFlyingAttacker() {
        addCreatureReady(player1, new SirenStormtamer());
        Permanent whiptail = addCreatureReady(player2, new GrazingWhiptail());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(whiptail.isBlocking()).isTrue();
        resolveCombat();

        harness.assertInGraveyard(player1, "Siren Stormtamer");
        harness.assertOnBattlefield(player2, "Grazing Whiptail");
        harness.assertLife(player2, 20);
    }

    @Test
    void reachAlsoAllowsBlockingNonflyingAttacker() {
        addCreatureReady(player1, new RaptorCompanion());
        Permanent whiptail = addCreatureReady(player2, new GrazingWhiptail());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(whiptail.isBlocking()).isTrue();
        resolveCombat();

        harness.assertInGraveyard(player1, "Raptor Companion");
        harness.assertOnBattlefield(player2, "Grazing Whiptail");
        harness.assertLife(player2, 20);
    }

    @Test
    void reachDoesNotPreventNonflyingCreatureFromBlockingWhiptail() {
        addCreatureReady(player1, new GrazingWhiptail());
        Permanent raptor = addCreatureReady(player2, new RaptorCompanion());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(raptor.isBlocking()).isTrue();
        resolveCombat();

        harness.assertOnBattlefield(player1, "Grazing Whiptail");
        harness.assertInGraveyard(player2, "Raptor Companion");
        harness.assertLife(player2, 20);
    }
}
