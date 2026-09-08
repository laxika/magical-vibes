package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NerivHeartOfTheStorm.class, ZuranSpellcaster.class})
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
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, java.util.List.of());
        harness.passBothPriorities();

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

    private Permanent addEnteredCreature() {
        Permanent spellcaster = harness.addToBattlefieldAndReturn(player1, new ZuranSpellcaster());
        gd.permanentsEnteredBattlefieldThisTurn
                .computeIfAbsent(player1.getId(), ignored -> new ArrayList<>())
                .add(spellcaster.getCard());
        return spellcaster;
    }
}
