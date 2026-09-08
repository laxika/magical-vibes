package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThrunBreakerOfSilenceTest extends BaseCardTest {

    @Test
    @DisplayName("Thrun cannot be countered")
    void cannotBeCountered() {
        ThrunBreakerOfSilence thrun = new ThrunBreakerOfSilence();
        harness.setHand(player1, List.of(thrun));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, thrun.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(thrun.getId()));
    }

    @Test
    @DisplayName("Thrun cannot be targeted by an opponent's nongreen spell")
    void opponentNongreenSpellCannotTarget() {
        Permanent thrun = addReadyThrun(player1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, thrun.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Thrun can be targeted by its controller's nongreen spell")
    void controllerNongreenSpellCanTarget() {
        Permanent thrun = addReadyThrun(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, thrun.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Thrun can be targeted by an opponent's green spell")
    void opponentGreenSpellCanTarget() {
        Permanent thrun = addReadyThrun(player1);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, thrun.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Thrun cannot be targeted by an opponent's nongreen ability")
    void opponentNongreenAbilityCannotTarget() {
        Permanent thrun = addReadyThrun(player1);
        harness.addToBattlefield(player2, new ProdigalPyromancer());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, thrun.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Thrun has indestructible during its controller's turn only")
    void indestructibleDuringControllerTurnOnly() {
        Permanent thrun = addReadyThrun(player1);
        thrun.setMarkedDamage(5);

        harness.forceActivePlayer(player1);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(thrun);

        harness.forceActivePlayer(player2);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(thrun);
    }

    private Permanent addReadyThrun(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new ThrunBreakerOfSilence());
    }
}
