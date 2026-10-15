package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.k.Kindle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BasandraBattleSeraph.class, AshcoatBear.class, GrizzlyBears.class, Kindle.class, Humility.class})
class BasandraBattleSeraphTest extends BaseCardTest {

    @Test
    @DisplayName("Players can't cast spells during combat")
    void playersCannotCastSpellsDuringCombat() {
        harness.addToBattlefield(player1, new BasandraBattleSeraph());
        harness.setHand(player1, List.of(new AshcoatBear()));
        harness.setHand(player2, List.of(new AshcoatBear()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Spells can be cast outside combat")
    void spellsCanBeCastOutsideCombat() {
        harness.addToBattlefield(player1, new BasandraBattleSeraph());
        harness.setHand(player1, List.of(new Kindle()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Red ability makes the target attack this turn if able")
    void abilityMakesTargetAttack() {
        harness.addToBattlefield(player1, new BasandraBattleSeraph());
        var target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isMustAttackThisTurn()).isTrue();
        assertThat(target.getMustAttackTargetId()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = TurnStep.class, names = {
            "BEGINNING_OF_COMBAT", "DECLARE_ATTACKERS", "DECLARE_BLOCKERS", "COMBAT_DAMAGE", "END_OF_COMBAT"
    })
    void neitherPlayerCanCastInstantsDuringAnyCombatStep(TurnStep step) {
        harness.addToBattlefield(player2, new BasandraBattleSeraph());
        harness.setHand(player1, List.of(new Kindle()));
        harness.setHand(player2, List.of(new Kindle()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(step);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void spellsCanBeCastAfterCombat() {
        harness.addToBattlefield(player1, new BasandraBattleSeraph());
        harness.setHand(player1, List.of(new Kindle()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    void tappedSummoningSickBasandraCanActivateDuringCombatAndTargetItself() {
        var basandra = harness.addToBattlefieldAndReturn(player1, new BasandraBattleSeraph());
        basandra.tap();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        harness.activateAbility(player1, 0, null, basandra.getId());
        harness.passBothPriorities();

        assertThat(basandra.isMustAttackThisTurn()).isTrue();
        assertThat(basandra.getMustAttackTargetId()).isNull();
    }

    @Test
    void ownReadyCreatureMustAttackAfterAbilityResolves() {
        var basandra = addCreatureReady(player1, new BasandraBattleSeraph());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, basandra.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        assertThat(basandra.isAttacking()).isTrue();
    }

    @Test
    void tappedCreatureIsNotRequiredToAttack() {
        var basandra = addCreatureReady(player1, new BasandraBattleSeraph());
        basandra.tap();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, basandra.getId());
        harness.passBothPriorities();

        declareAttackers(player1, List.of());

        assertThat(basandra.isAttackedThisTurn()).isFalse();
    }

    @Test
    void summoningSickCreatureIsNotRequiredToAttack() {
        var basandra = harness.addToBattlefieldAndReturn(player1, new BasandraBattleSeraph());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, basandra.getId());
        harness.passBothPriorities();

        declareAttackers(player1, List.of());

        assertThat(basandra.isAttackedThisTurn()).isFalse();
    }

    @Test
    void attackRequirementExpiresAtEndOfTurn() {
        var basandra = addCreatureReady(player1, new BasandraBattleSeraph());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, basandra.getId());
        harness.passBothPriorities();
        assertThat(basandra.isMustAttackThisTurn()).isTrue();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(basandra.isMustAttackThisTurn()).isFalse();
    }

    @Test
    void activatedAbilityResolvesAfterBasandraLeavesBattlefield() {
        var basandra = harness.addToBattlefieldAndReturn(player1, new BasandraBattleSeraph());
        var target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(basandra);

        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void abilityDoesNotAffectTargetThatLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new BasandraBattleSeraph());
        var target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.isMustAttackThisTurn()).isFalse();
    }

    @Test
    void humilityRemovesBasandrasCombatSpellRestriction() {
        harness.addToBattlefield(player1, new BasandraBattleSeraph());
        harness.addToBattlefield(player2, new Humility());
        harness.setHand(player1, List.of(new Kindle()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
    }
}
