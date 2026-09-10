package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.cards.w.WuInfantry;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({EightfoldMaze.class, WuInfantry.class, NicolBolasPlaneswalker.class})
class EightfoldMazeTest extends BaseCardTest {

    @Test
    @DisplayName("Cast during declare attackers while attacked: destroys the attacker")
    void destroysAttacker() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addAttacker(player1, player2, new WuInfantry());
        harness.setHand(player2, List.of(new EightfoldMaze()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player2, 0, attacker.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Wu Infantry");
        harness.assertInGraveyard(player1, "Wu Infantry");
    }

    @Test
    @DisplayName("Cannot cast during declare attackers if you have not been attacked")
    void cannotCastWhenNotAttacked() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addAttacker(player1, player1, new WuInfantry());
        harness.setHand(player2, List.of(new EightfoldMaze()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast when only a planeswalker you control is attacked")
    void cannotCastWhenOnlyPlaneswalkerIsAttacked() {
        harness.forceActivePlayer(player1);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new NicolBolasPlaneswalker());
        Permanent attacker = addAttacker(player1, player2, new WuInfantry());
        attacker.setAttackTarget(planeswalker.getId());
        harness.setHand(player2, List.of(new EightfoldMaze()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttacker() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2, new WuInfantry());
        Permanent nonAttacker = harness.addToBattlefieldAndReturn(player2, new WuInfantry());
        harness.setHand(player2, List.of(new EightfoldMaze()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature");
    }

    @Test
    @DisplayName("Fizzles if the target stops attacking before resolution")
    void fizzlesIfTargetStopsAttackingBeforeResolution() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addAttacker(player1, player2, new WuInfantry());
        harness.setHand(player2, List.of(new EightfoldMaze()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castInstant(player2, 0, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Wu Infantry");
        harness.assertNotInGraveyard(player1, "Wu Infantry");
    }

    @Test
    @DisplayName("Cannot cast outside the declare attackers step")
    void cannotCastOutsideDeclareAttackers() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addAttacker(player1, player2, new WuInfantry());
        harness.setHand(player2, List.of(new EightfoldMaze()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private Permanent addAttacker(Player controller, Player defender, Card card) {
        Permanent perm = addCreatureReady(controller, card);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        return perm;
    }
}
