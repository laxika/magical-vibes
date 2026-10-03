package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DulcetSirens.class, GrizzlyBears.class})
class DulcetSirensTest extends BaseCardTest {

    @Test
    @DisplayName("Forces a target creature to attack the chosen opponent this turn")
    void forcesCreatureToAttackChosenOpponent() {
        Permanent sirens = addCreatureReady(player1, new DulcetSirens());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbilityWithMultiTargets(player1, battlefieldIndex(sirens), 0,
                List.of(creature.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(creature.isMustAttackThisTurn()).isTrue();
        assertThat(creature.getMustAttackTargetId()).isEqualTo(player2.getId());

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);

        declareAttackers(player1, List.of(battlefieldIndex(creature)));
        assertThat(creature.isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Only an opponent can be chosen as the attack target")
    void requiresOpponentTarget() {
        Permanent sirens = addCreatureReady(player1, new DulcetSirens());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, battlefieldIndex(sirens), 0,
                List.of(creature.getId(), player1.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    void tappedCreatureIsNotRequiredToAttack() {
        Permanent sirens = addCreatureReady(player1, new DulcetSirens());
        Permanent creature = addCreatureReady(player1, new DulcetSirens());
        creature.setTapped(true);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbilityWithMultiTargets(player1, battlefieldIndex(sirens), 0,
                List.of(creature.getId(), player2.getId()));
        harness.passBothPriorities();

        declareAttackers(player1, List.of());
        assertThat(creature.isAttackedThisTurn()).isFalse();
    }

    @Test
    void creatureCannotBeRequiredToAttackItsOwnController() {
        Permanent sirens = addCreatureReady(player1, new DulcetSirens());
        Permanent creature = addCreatureReady(player2, new DulcetSirens());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbilityWithMultiTargets(player1, battlefieldIndex(sirens), 0,
                List.of(creature.getId(), player2.getId()));
        harness.passBothPriorities();

        declareAttackers(player2, List.of());
        assertThat(creature.isAttackedThisTurn()).isFalse();
    }

    @Test
    void creatureLeavingBeforeResolutionDoesNotAffectAnotherCreature() {
        Permanent sirens = addCreatureReady(player1, new DulcetSirens());
        Permanent creature = addCreatureReady(player1, new DulcetSirens());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbilityWithMultiTargets(player1, battlefieldIndex(sirens), 0,
                List.of(creature.getId(), player2.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        harness.passBothPriorities();

        declareAttackers(player1, List.of());
        assertThat(sirens.isMustAttackThisTurn()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void morphCanBeTurnedFaceUpForOneBlueAndUseItsAbility() {
        harness.setHand(player1, List.of(new DulcetSirens()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        Permanent sirens = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(sirens.isFaceDown()).isTrue();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, battlefieldIndex(sirens));
        assertThat(sirens.isFaceDown()).isFalse();

        sirens.setSummoningSick(false);
        Permanent creature = addCreatureReady(player1, new DulcetSirens());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbilityWithMultiTargets(player1, battlefieldIndex(sirens), 0,
                List.of(creature.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(sirens.isTapped()).isTrue();
        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        declareAttackers(player1, List.of(battlefieldIndex(creature)));
        assertThat(creature.isAttackedThisTurn()).isTrue();
    }

    private int battlefieldIndex(Permanent permanent) {
        for (Player player : List.of(player1, player2)) {
            int index = gd.playerBattlefields.get(player.getId()).indexOf(permanent);
            if (index >= 0) {
                return index;
            }
        }
        throw new IllegalStateException("Permanent not found on battlefield");
    }
}
