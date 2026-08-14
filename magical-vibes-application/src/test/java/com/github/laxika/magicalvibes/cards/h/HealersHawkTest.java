package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HealersHawkTest extends BaseCardTest {

    @Test
    @DisplayName("Has flying and lifelink")
    void hasFlyingAndLifelink() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new HealersHawk());

        assertThat(gqs.hasKeyword(gd, hawk, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, hawk, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Gains life equal to combat damage dealt")
    void gainsLifeFromCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new HealersHawk());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
