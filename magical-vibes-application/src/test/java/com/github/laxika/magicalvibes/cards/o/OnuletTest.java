package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OnuletTest extends BaseCardTest {

    @Test
    @DisplayName("Onulet dies from Wrath of God, controller gains 2 life")
    void diesFromWrathGainsLife() {
        harness.addToBattlefield(player1, new Onulet());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        GameData gd = harness.getGameData();
        int lifeBefore = gd.getLife(player1.getId());

        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        // Onulet is dead
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Onulet"));

        // Resolve the death trigger from the stack
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Onulet dies in combat, controller gains 2 life")
    void diesInCombatGainsLife() {
        Onulet onulet = new Onulet();
        Permanent onuletPerm = new Permanent(onulet);
        onuletPerm.setSummoningSick(false);
        onuletPerm.setBlocking(true);
        onuletPerm.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(onuletPerm);

        GrizzlyBears bears = new GrizzlyBears();
        bears.setPower(3);
        bears.setToughness(3);
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        GameData gd = harness.getGameData();
        int lifeBefore = gd.getLife(player1.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Onulet"));

        // Resolve the death trigger from the stack
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Onulet survives combat, no life gained")
    void survivesNoLifeGain() {
        Onulet onulet = new Onulet();
        Permanent onuletPerm = new Permanent(onulet);
        onuletPerm.setSummoningSick(false);
        onuletPerm.setBlocking(true);
        onuletPerm.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(onuletPerm);

        GrizzlyBears weakAttacker = new GrizzlyBears();
        weakAttacker.setPower(0);
        weakAttacker.setToughness(2);
        Permanent attacker = new Permanent(weakAttacker);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        GameData gd = harness.getGameData();
        int lifeBefore = gd.getLife(player1.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Onulet"));
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }
}
