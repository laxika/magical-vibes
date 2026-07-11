package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CoordinatedBarrageTest extends BaseCardTest {

    /** Puts an attacking creature on player1's battlefield and hands player2 the spell + {W}. */
    private Permanent setupAttackerAndSpell(Permanent attacker) {
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CoordinatedBarrage()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);
        return attacker;
    }

    @Test
    @DisplayName("Deals damage equal to the number of permanents of the chosen type you control")
    void damageEqualsChosenTypeCount() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent target = setupAttackerAndSpell(new Permanent(new AirElemental()));

        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "BEAR");

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Air Elemental") && p.getMarkedDamage() == 2);
    }

    @Test
    @DisplayName("Lethal damage from the chosen-type count destroys the target")
    void lethalCountKillsTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent target = setupAttackerAndSpell(new Permanent(new HillGiant()));

        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "BEAR");

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Hill Giant"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Hill Giant"));
    }

    @Test
    @DisplayName("Choosing a type you control none of deals no damage")
    void chosenTypeYouControlNoneDealsZero() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent target = setupAttackerAndSpell(new Permanent(new AirElemental()));

        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "GOBLIN");

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Air Elemental") && p.getMarkedDamage() == 0);
    }

    @Test
    @DisplayName("A Changeling you control counts as the chosen type")
    void changelingCountsAsChosenType() {
        harness.addToBattlefield(player2, new AvianChangeling());

        Permanent target = setupAttackerAndSpell(new Permanent(new AirElemental()));

        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "GOBLIN");

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Air Elemental") && p.getMarkedDamage() == 1);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CoordinatedBarrage()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }
}
