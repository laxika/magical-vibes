package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JurinLeadingTheCharge.class, GrizzlyBears.class})
class JurinLeadingTheChargeTest extends BaseCardTest {

    @Test
    @DisplayName("Jurin boosts creatures attacking a player by that player's creature count")
    void boostsAttackersByDefendingCreatureCount() {
        addCreatureReady(player1, new JurinLeadingTheCharge());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent defenderOne = addCreatureReady(player2, new GrizzlyBears());
        Permanent defenderTwo = addCreatureReady(player2, new GrizzlyBears());
        defenderOne.tap();
        defenderTwo.tap();

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, gd.playerBattlefields.get(player1.getId()).get(0)))
                .isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
    }

    @Test
    @DisplayName("Jurin's attack ability does not trigger when only another creature attacks")
    void onlyTriggersWhenJurinAttacks() {
        addCreatureReady(player1, new JurinLeadingTheCharge());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent defender = addCreatureReady(player2, new GrizzlyBears());
        defender.tap();

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Jurin must be blocked if able")
    void mustBeBlockedIfAble() {
        addCreatureReady(player1, new JurinLeadingTheCharge());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");
    }
}
