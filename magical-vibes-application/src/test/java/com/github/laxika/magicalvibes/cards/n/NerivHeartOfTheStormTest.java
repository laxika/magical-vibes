package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NerivHeartOfTheStorm.class, ZuranSpellcaster.class, Humble.class})
class NerivHeartOfTheStormTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles combat damage from a creature that entered this turn")
    void doublesCombatDamageFromCreatureEnteredThisTurn() {
        harness.addToBattlefield(player1, new NerivHeartOfTheStorm());
        Permanent attacker = addEnteredCreature();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.resolveCombatDamage();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Doubles noncombat damage from a creature that entered this turn")
    void doublesNoncombatDamageFromCreatureEnteredThisTurn() {
        harness.addToBattlefield(player1, new NerivHeartOfTheStorm());
        Permanent spellcaster = addEnteredCreature();
        spellcaster.setSummoningSick(false);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not double damage from a creature that did not enter this turn")
    void doesNotDoubleDamageFromCreatureThatDidNotEnterThisTurn() {
        harness.addToBattlefield(player1, new NerivHeartOfTheStorm());
        Permanent spellcaster = harness.addToBattlefieldAndReturn(player1, new ZuranSpellcaster());
        spellcaster.setSummoningSick(false);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Neriv doubles its own damage on the turn it enters")
    void doublesItsOwnDamage() {
        Permanent neriv = harness.enterBattlefieldAndReturn(player1, new NerivHeartOfTheStorm());
        neriv.setSummoningSick(false);
        neriv.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("Does not double an opponent's newly entered creature damage")
    void doesNotDoubleOpponentsDamage() {
        harness.addToBattlefield(player1, new NerivHeartOfTheStorm());
        Permanent spellcaster = harness.enterBattlefieldAndReturn(player2, new ZuranSpellcaster());
        spellcaster.setSummoningSick(false);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Doubles damage to a creature as well as to players")
    void doublesDamageToCreature() {
        harness.addToBattlefield(player1, new NerivHeartOfTheStorm());
        Permanent spellcaster = addEnteredCreature();
        spellcaster.setSummoningSick(false);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NerivHeartOfTheStorm());

        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Checks current control even when the damage ability was activated by another player")
    void doesNotDoubleDamageAfterSourceChangesController() {
        harness.addToBattlefield(player1, new NerivHeartOfTheStorm());
        Permanent spellcaster = addEnteredCreature();
        spellcaster.setSummoningSick(false);
        harness.activateAbility(player1, 1, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(spellcaster);
        gd.playerBattlefields.get(player2.getId()).add(spellcaster);

        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("A creature that entered under another player's control still qualifies after control changes")
    void doublesDamageAfterGainingNewlyEnteredCreature() {
        harness.addToBattlefield(player1, new NerivHeartOfTheStorm());
        Permanent spellcaster = harness.enterBattlefieldAndReturn(player2, new ZuranSpellcaster());
        gd.playerBattlefields.get(player2.getId()).remove(spellcaster);
        gd.playerBattlefields.get(player1.getId()).add(spellcaster);
        spellcaster.setSummoningSick(false);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Neriv stops doubling damage when it loses all abilities")
    void doesNotDoubleDamageAfterLosingAbilities() {
        Permanent neriv = harness.addToBattlefieldAndReturn(player1, new NerivHeartOfTheStorm());
        Permanent spellcaster = addEnteredCreature();
        spellcaster.setSummoningSick(false);
        Humble humble = new Humble();
        harness.setHand(player2, java.util.List.of(humble));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, neriv.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Neriv stops doubling combat damage when it loses all abilities")
    void doesNotDoubleCombatDamageAfterLosingAbilities() {
        Permanent neriv = harness.addToBattlefieldAndReturn(player1, new NerivHeartOfTheStorm());
        Permanent attacker = addEnteredCreature();
        attacker.setSummoningSick(false);
        harness.setHand(player2, java.util.List.of(new Humble()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, neriv.getId());
        harness.passBothPriorities();
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 19);
    }

    private Permanent addEnteredCreature() {
        return harness.enterBattlefieldAndReturn(player1, new ZuranSpellcaster());
    }
}
