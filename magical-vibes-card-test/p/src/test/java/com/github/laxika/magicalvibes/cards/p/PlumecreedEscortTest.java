package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlumecreedEscort.class})
class PlumecreedEscortTest extends BaseCardTest {

    @Test
    void grantsHexproofToTargetCreatureUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PlumecreedEscort());
        harness.setHand(player1, List.of(new PlumecreedEscort()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0, 0, target.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void cannotTargetCreatureControlledByOpponent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PlumecreedEscort());
        harness.setHand(player1, List.of(new PlumecreedEscort()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    void canTargetItselfWhenEnteringAnEmptyBattlefield() {
        Permanent escort = harness.enterBattlefieldAndReturn(player1, new PlumecreedEscort());

        harness.handlePermanentChosen(player1, escort.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, escort, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void triggerStillGrantsHexproofAfterSourceDies() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PlumecreedEscort());
        Permanent source = harness.enterBattlefieldAndReturn(player1, new PlumecreedEscort());
        harness.handlePermanentChosen(player1, target.getId());

        source.setMarkedDamage(1);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
    }
}
