package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SicarianInfiltrator;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChaosTerminatorLord.class, SicarianInfiltrator.class})
class ChaosTerminatorLordTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of combat on your turn, another creature you control gains double strike")
    void grantsDoubleStrikeAtBeginningOfCombat() {
        harness.addToBattlefield(player1, new ChaosTerminatorLord());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new SicarianInfiltrator());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, ally.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ally, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Double strike wears off at end of turn")
    void doubleStrikeWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new ChaosTerminatorLord());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new SicarianInfiltrator());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, ally.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ally, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentCombat() {
        harness.addToBattlefield(player1, new ChaosTerminatorLord());
        harness.addToBattlefield(player1, new SicarianInfiltrator());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target itself or an opponent's creature")
    void cannotTargetSelfOrOpponentCreature() {
        Permanent lord = harness.addToBattlefieldAndReturn(player1, new ChaosTerminatorLord());
        Permanent enemy = harness.addToBattlefieldAndReturn(player2, new SicarianInfiltrator());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new SicarianInfiltrator());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, lord.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, enemy.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, ally.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The combat ability resolves even if its source leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent lord = harness.addToBattlefieldAndReturn(player1, new ChaosTerminatorLord());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new SicarianInfiltrator());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, ally.getId());
        gd.playerBattlefields.get(player1.getId()).remove(lord);
        gd.playerGraveyards.get(player1.getId()).add(lord.getCard());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ally, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The target must still be controlled by you when the ability resolves")
    void doesNotGrantDoubleStrikeAfterTargetChangesController() {
        harness.addToBattlefield(player1, new ChaosTerminatorLord());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new SicarianInfiltrator());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, ally.getId());
        gd.playerBattlefields.get(player1.getId()).remove(ally);
        gd.playerBattlefields.get(player2.getId()).add(ally);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ally, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability does not affect a creature that left the battlefield")
    void doesNotGrantDoubleStrikeAfterTargetLeavesBattlefield() {
        harness.addToBattlefield(player1, new ChaosTerminatorLord());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new SicarianInfiltrator());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, ally.getId());
        gd.playerBattlefields.get(player1.getId()).remove(ally);
        gd.playerGraveyards.get(player1.getId()).add(ally.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("With no other creature you control, the ability has no legal target")
    void noLegalTargetDoesNotLeaveAnUnresolvableAbility() {
        Permanent lord = harness.addToBattlefieldAndReturn(player1, new ChaosTerminatorLord());
        harness.addToBattlefield(player2, new SicarianInfiltrator());

        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.hasKeyword(gd, lord, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
