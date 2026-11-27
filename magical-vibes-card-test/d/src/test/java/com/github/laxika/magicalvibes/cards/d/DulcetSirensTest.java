package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.effect.MustAttackIfAbleEffect;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeylineOfSanctity;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DulcetSirens.class, GrizzlyBears.class, LeylineOfSanctity.class})
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

        assertThat(gd.hasFloatingEffectOnPermanent(creature.getId(), MustAttackIfAbleEffect.class)).isTrue();
        assertThat(gd.mustAttackDefender(creature.getId())).isEqualTo(player2.getId());

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
    @DisplayName("An opponent who became an illegal target leaves the creature free to attack anyone or not at all")
    void illegalOpponentTargetRequiresNothing() {
        Permanent sirens = addCreatureReady(player1, new DulcetSirens());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbilityWithMultiTargets(player1, battlefieldIndex(sirens), 0,
                List.of(creature.getId(), player2.getId()));
        harness.addToBattlefield(player2, new LeylineOfSanctity());
        harness.passBothPriorities();

        assertThat(gd.hasFloatingEffectOnPermanent(creature.getId(), MustAttackIfAbleEffect.class)).isFalse();
        declareAttackers(player1, List.of());
        assertThat(creature.isAttackedThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The resolution logs which player the creature must attack")
    void logsThePlayerToAttack() {
        Permanent sirens = addCreatureReady(player1, new DulcetSirens());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbilityWithMultiTargets(player1, battlefieldIndex(sirens), 0,
                List.of(creature.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(line -> line.plainText().contains("Grizzly Bears must attack "
                + gd.playerIdToName.get(player2.getId()) + " this turn if able."));
    }

    @Nested
    @DisplayName("With two opponents, each named by one requirement")
    class TwoRequirementsNamingDifferentOpponents {

        private Player player3;
        private Permanent creature;

        @BeforeEach
        void forceTheCreatureToAttackEachOpponent() {
            player3 = harness.addOpponent("Player3");
            Permanent first = addCreatureReady(player1, new DulcetSirens());
            Permanent second = addCreatureReady(player1, new DulcetSirens());
            creature = addCreatureReady(player1, new GrizzlyBears());
            harness.addMana(player1, ManaColor.BLUE, 2);
            harness.activateAbilityWithMultiTargets(player1, battlefieldIndex(first), 0,
                    List.of(creature.getId(), player2.getId()));
            harness.passBothPriorities();
            harness.activateAbilityWithMultiTargets(player1, battlefieldIndex(second), 0,
                    List.of(creature.getId(), player3.getId()));
            harness.passBothPriorities();
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);
            harness.clearPriorityPassed();
            harness.beginAttackerDeclarationInput();
        }

        @Test
        @DisplayName("Attacking the opponent the first requirement names obeys as many requirements as possible")
        void attackingTheFirstNamedOpponentIsLegal() {
            int index = battlefieldIndex(creature);
            harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                    () -> gs.declareAttackers(gd, player1, List.of(index), Map.of(index, player2.getId())));

            assertThat(creature.getAttackTarget()).isEqualTo(player2.getId());
        }

        @Test
        @DisplayName("Attacking the opponent the second requirement names is legal too")
        void attackingTheSecondNamedOpponentIsLegal() {
            int index = battlefieldIndex(creature);
            harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                    () -> gs.declareAttackers(gd, player1, List.of(index), Map.of(index, player3.getId())));

            assertThat(creature.getAttackTarget()).isEqualTo(player3.getId());
        }

        @Test
        @DisplayName("The creature still can't stay home")
        void creatureCannotStayHome() {
            assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(), Map.of()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("must attack");
        }
    }

    @Test
    @DisplayName("With two opponents, attacking the opponent who isn't named obeys too few requirements")
    void attackingTheUnnamedOpponentIsIllegal() {
        Player player3 = harness.addOpponent("Player3");
        Permanent sirens = addCreatureReady(player1, new DulcetSirens());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbilityWithMultiTargets(player1, battlefieldIndex(sirens), 0,
                List.of(creature.getId(), player2.getId()));
        harness.passBothPriorities();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int index = battlefieldIndex(creature);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(index), Map.of(index, player3.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too few attack requirements");
    }

    @Test
    @DisplayName("A tapped creature is not required to attack")
    void tappedCreatureIsNotRequiredToAttack() {
        Permanent sirens = addCreatureReady(player1, new DulcetSirens());
        Permanent creature = addCreatureReady(player1, new DulcetSirens());
        creature.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbilityWithMultiTargets(player1, battlefieldIndex(sirens), 0,
                List.of(creature.getId(), player2.getId()));
        harness.passBothPriorities();

        declareAttackers(player1, List.of());
        assertThat(creature.isAttackedThisTurn()).isFalse();
    }

    @Test
    @DisplayName("An opponent's creature can't be made to attack its own controller")
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
    @DisplayName("A target creature that left doesn't pass the requirement to another creature")
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
        assertThat(gd.hasFloatingEffectOnPermanent(sirens.getId(), MustAttackIfAbleEffect.class)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Turned face up for its morph cost, it can use its ability")
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
