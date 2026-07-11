package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WarriorsStandTest extends BaseCardTest {

    @Test
    @DisplayName("Cast during declare attackers while attacked: creatures you control get +2/+2")
    void boostsOwnCreaturesWhenAttacked() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new WarriorsStand()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        for (Permanent p : gd.playerBattlefields.get(player2.getId())) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(p.getEffectivePower()).isEqualTo(4);
                assertThat(p.getEffectiveToughness()).isEqualTo(4);
            }
        }
    }

    @Test
    @DisplayName("Does not boost the opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new WarriorsStand()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        for (Permanent p : gd.playerBattlefields.get(player1.getId())) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(p.getPowerModifier()).isEqualTo(0);
                assertThat(p.getToughnessModifier()).isEqualTo(0);
            }
        }
    }

    @Test
    @DisplayName("Cannot cast during declare attackers if not attacked")
    void cannotCastWhenNotAttacked() {
        harness.forceActivePlayer(player1);
        // Attacker aims at nobody the caster controls.
        addAttacker(player1, player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new WarriorsStand()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast outside the declare attackers step")
    void cannotCastOutsideDeclareAttackers() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new WarriorsStand()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private Permanent addAttacker(Player attackerController, Player defender) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        gd.playerBattlefields.get(attackerController.getId()).add(perm);
        return perm;
    }
}
