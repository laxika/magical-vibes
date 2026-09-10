package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MoggConscripts;
import com.github.laxika.magicalvibes.cards.s.SoltariFootSoldier;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Opportunist.class, SoltariFootSoldier.class, MoggConscripts.class, Forest.class})
class OpportunistTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a creature that was dealt damage this turn, killing a 1/1")
    void deals1DamageToDamagedCreature() {
        Permanent opportunist = addCreatureReady(player1, new Opportunist());
        harness.addToBattlefield(player2, new SoltariFootSoldier());

        UUID targetId = harness.getPermanentId(player2, "Soltari Foot Soldier");
        gd.permanentsDealtDamageThisTurn.add(targetId);

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(opportunist.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Soltari Foot Soldier");
        harness.assertInGraveyard(player2, "Soltari Foot Soldier");
    }

    @Test
    @DisplayName("A 2/2 already dealt damage survives the extra 1 damage")
    void damagedToughCreatureSurvives() {
        addCreatureReady(player1, new Opportunist());
        harness.addToBattlefield(player2, new MoggConscripts());

        UUID targetId = harness.getPermanentId(player2, "Mogg Conscripts");
        gd.permanentsDealtDamageThisTurn.add(targetId);

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Mogg Conscripts");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target your own creature that was dealt damage this turn")
    void canTargetOwnDamagedCreature() {
        addCreatureReady(player1, new Opportunist());
        harness.addToBattlefield(player1, new SoltariFootSoldier());

        UUID targetId = harness.getPermanentId(player1, "Soltari Foot Soldier");
        gd.permanentsDealtDamageThisTurn.add(targetId);

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Soltari Foot Soldier");
    }

    @Test
    @DisplayName("Cannot target a creature that was not dealt damage this turn")
    void cannotTargetUndamagedCreature() {
        addCreatureReady(player1, new Opportunist());
        harness.addToBattlefield(player2, new MoggConscripts());

        UUID targetId = harness.getPermanentId(player2, "Mogg Conscripts");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dealt damage this turn");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent even if it was dealt damage this turn")
    void cannotTargetDamagedNoncreaturePermanent() {
        addCreatureReady(player1, new Opportunist());
        harness.addToBattlefield(player2, new Forest());

        UUID targetId = harness.getPermanentId(player2, "Forest");
        gd.permanentsDealtDamageThisTurn.add(targetId);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new Opportunist());
        harness.addToBattlefield(player2, new MoggConscripts());

        UUID targetId = harness.getPermanentId(player2, "Mogg Conscripts");
        gd.permanentsDealtDamageThisTurn.add(targetId);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }
}
