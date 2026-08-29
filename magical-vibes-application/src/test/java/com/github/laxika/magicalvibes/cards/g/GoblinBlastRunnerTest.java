package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.Atog;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinBlastRunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 and menace after you sacrifice a permanent")
    void getsBonusAfterSacrifice() {
        harness.addToBattlefield(player1, new Atog());
        harness.addToBattlefield(player1, new GoblinBlastRunner());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent blastRunner = findPermanent(player1, "Goblin Blast-Runner");
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blastRunner)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, blastRunner)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, blastRunner, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("The bonus ends with the turn")
    void bonusEndsWithTurn() {
        harness.addToBattlefield(player1, new Atog());
        harness.addToBattlefield(player1, new GoblinBlastRunner());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent blastRunner = findPermanent(player1, "Goblin Blast-Runner");
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, blastRunner)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blastRunner)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, blastRunner, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's sacrifice does not grant the bonus")
    void opponentSacrificeDoesNotGrantBonus() {
        harness.addToBattlefield(player1, new GoblinBlastRunner());
        harness.addToBattlefield(player2, new Atog());
        harness.addToBattlefield(player2, new Spellbook());

        Permanent blastRunner = findPermanent(player1, "Goblin Blast-Runner");
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blastRunner)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, blastRunner, Keyword.MENACE)).isFalse();
    }
}
