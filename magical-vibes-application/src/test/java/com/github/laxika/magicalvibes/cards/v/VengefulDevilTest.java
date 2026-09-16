package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VengefulDevil.class, GrizzlyBears.class, LlanowarElves.class, Shock.class})
class VengefulDevilTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate without a creature having died this turn")
    void cannotActivateWithoutMorbid() {
        Permanent devil = addReadyDevil(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(devil), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Morbid");
    }

    @Test
    @DisplayName("Deals 1 damage to a target player when morbid is met")
    void dealsDamageToTargetPlayerWithMorbid() {
        harness.setLife(player2, 20);
        Permanent devil = addReadyDevil(player1);
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(devil), null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(devil.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Deals 1 damage to a target creature")
    void dealsDamageToTargetCreature() {
        Permanent devil = addReadyDevil(player1);
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(devil), null, elves.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("An actual creature death enables the ability")
    void actualCreatureDeathEnablesMorbid() {
        Permanent devil = addReadyDevil(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(devil), null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    private Permanent addReadyDevil(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        Permanent devil = harness.addToBattlefieldAndReturn(player, new VengefulDevil());
        devil.setSummoningSick(false);
        return devil;
    }
}
