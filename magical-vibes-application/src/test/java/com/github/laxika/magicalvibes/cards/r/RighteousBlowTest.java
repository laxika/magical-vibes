package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class RighteousBlowTest extends BaseCardTest {

    /** Puts a combat creature on player1's battlefield and hands player2 the spell + {W}. */
    private Permanent setupCombatantAndSpell(Permanent combatant, boolean attacking) {
        combatant.setSummoningSick(false);
        if (attacking) {
            combatant.setAttacking(true);
        } else {
            combatant.setBlocking(true);
        }
        harness.getGameData().playerBattlefields.get(player1.getId()).add(combatant);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new RighteousBlow()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);
        return combatant;
    }

    @Test
    @DisplayName("Deals 2 damage to an attacking creature")
    void dealsTwoDamageToAttacker() {
        Permanent target = setupCombatantAndSpell(new Permanent(new AirElemental()), true);

        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Air Elemental") && p.getMarkedDamage() == 2);
    }

    @Test
    @DisplayName("Two damage destroys a blocking creature with toughness 2")
    void lethalDamageKillsBlocker() {
        Permanent target = setupCombatantAndSpell(new Permanent(new GrizzlyBears()), false);

        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        harness.addToBattlefield(player1, new AirElemental());
        UUID targetId = harness.getPermanentId(player1, "Air Elemental");

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new RighteousBlow()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }
}
