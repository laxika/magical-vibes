package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({WakestoneGargoyle.class, WallOfWood.class, GrizzlyBears.class})
class WakestoneGargoyleTest extends BaseCardTest {

    @Test
    @DisplayName("Defender creatures cannot attack before the ability resolves")
    void defenderCreaturesCannotAttackBeforeActivation() {
        addReady(player1, new WakestoneGargoyle());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("The ability lets your defender creatures attack")
    void abilityLetsYourDefenderCreaturesAttack() {
        Permanent gargoyle = addReady(player1, new WakestoneGargoyle());
        Permanent wall = addReady(player1, new WallOfWood());
        harness.addToBattlefield(player2, new GrizzlyBears());
        activateAbility();

        declareAttackers(List.of(0, 1));

        assertThat(gargoyle.isAttacking()).isTrue();
        assertThat(wall.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("The ability also affects a defender that enters later this turn")
    void abilityAffectsLaterDefender() {
        addReady(player1, new WakestoneGargoyle());
        harness.addToBattlefield(player2, new GrizzlyBears());
        activateAbility();
        Permanent wall = addReady(player1, new WallOfWood());

        declareAttackers(List.of(0, 1));

        assertThat(wall.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("The ability does not affect an opponent's defender creature")
    void abilityDoesNotAffectOpponentDefender() {
        addReady(player1, new WakestoneGargoyle());
        addReady(player2, new WallOfWood());
        activateAbility();

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("The attack permission expires at end of turn")
    void attackPermissionExpiresAtEndOfTurn() {
        addReady(player1, new WakestoneGargoyle());
        activateAbility();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    private void activateAbility() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
