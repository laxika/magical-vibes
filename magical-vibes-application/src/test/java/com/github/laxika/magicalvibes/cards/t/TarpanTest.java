package com.github.laxika.magicalvibes.cards.t;

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

class TarpanTest extends BaseCardTest {

    @Test
    @DisplayName("Tarpan dies from Wrath of God, controller gains 1 life")
    void diesFromWrathGainsLife() {
        harness.addToBattlefield(player1, new Tarpan());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        GameData gd = harness.getGameData();
        int lifeBefore = gd.getLife(player1.getId());

        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        // Tarpan is dead
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Tarpan"));

        // Resolve the death trigger from the stack
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Tarpan dies in combat, controller gains 1 life")
    void diesInCombatGainsLife() {
        Tarpan tarpan = new Tarpan();
        Permanent tarpanPerm = new Permanent(tarpan);
        tarpanPerm.setSummoningSick(false);
        tarpanPerm.setBlocking(true);
        tarpanPerm.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(tarpanPerm);

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
                .anyMatch(c -> c.getName().equals("Tarpan"));

        // Resolve the death trigger from the stack
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Tarpan survives combat, no life gained")
    void survivesNoLifeGain() {
        Tarpan tarpan = new Tarpan();
        Permanent tarpanPerm = new Permanent(tarpan);
        tarpanPerm.setSummoningSick(false);
        tarpanPerm.setBlocking(true);
        tarpanPerm.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(tarpanPerm);

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
                .anyMatch(p -> p.getCard().getName().equals("Tarpan"));
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }
}
