package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FomoriNomad;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StreetWraith.class, FomoriNomad.class, Swamp.class})
class StreetWraithTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling pays 2 life, discards Street Wraith, and draws a card")
    void cyclingPaysLifeDiscardsAndDraws() {
        harness.setHand(player1, List.of(new StreetWraith()));
        harness.setLibrary(player1, List.of(new FomoriNomad()));

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player1, "Street Wraith");
        harness.assertInHand(player1, "Fomori Nomad");
    }

    @Test
    @DisplayName("Street Wraith cannot be blocked while its defending player controls a Swamp")
    void cannotBeBlockedWhenDefenderControlsSwamp() {
        harness.addToBattlefield(player2, new Swamp());

        Permanent blockerPerm = addCreatureReady(player2, new FomoriNomad());

        Permanent attackerPerm = addCreatureReady(player1, new StreetWraith());
        attackerPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attackerPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Street Wraith can be blocked when its defending player controls no Swamp")
    void canBeBlockedWithoutSwamp() {
        Permanent blockerPerm = addCreatureReady(player2, new FomoriNomad());

        Permanent attackerPerm = addCreatureReady(player1, new StreetWraith());
        attackerPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attackerPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cycling cannot be activated without enough life to pay its cost")
    void cyclingRequiresTwoLife() {
        harness.setLife(player1, 1);
        harness.setHand(player1, List.of(new StreetWraith()));
        harness.setLibrary(player1, List.of(new FomoriNomad()));

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life to pay");

        harness.assertLife(player1, 1);
        harness.assertInHand(player1, "Street Wraith");
    }
}
