package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.ExpeditionHealer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LuminarchAspirant.class, ExpeditionHealer.class})
class LuminarchAspirantTest extends BaseCardTest {

    @Test
    @DisplayName("Beginning of combat puts a +1/+1 counter on a target creature you control")
    void beginningOfCombatPutsCounterOnTargetCreature() {
        addCreatureReady(player1, new LuminarchAspirant());
        Permanent healer = addCreatureReady(player1, new ExpeditionHealer());

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, healer.getId());
        harness.passBothPriorities();

        assertThat(healer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Beginning of combat cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        addCreatureReady(player1, new LuminarchAspirant());
        Permanent opponentHealer = addCreatureReady(player2, new ExpeditionHealer());

        advanceToBeginningOfCombat(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(opponentHealer.getId());
    }

    @Test
    @DisplayName("Does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentCombat() {
        addCreatureReady(player1, new LuminarchAspirant());
        addCreatureReady(player1, new ExpeditionHealer());

        advanceToBeginningOfCombat(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target itself and does not put the counter on until resolution")
    void canTargetItself() {
        Permanent aspirant = addCreatureReady(player1, new LuminarchAspirant());

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, aspirant.getId());

        assertThat(aspirant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(aspirant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers again when another combat begins during the same turn")
    void triggersInEachCombatOfItsControllersTurn() {
        Permanent aspirant = addCreatureReady(player1, new LuminarchAspirant());

        for (int combat = 0; combat < 2; combat++) {
            advanceToBeginningOfCombat(player1);
            harness.handlePermanentChosen(player1, aspirant.getId());
            harness.passBothPriorities();
            assertThat(aspirant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(combat + 1);
        }
    }

    @Test
    @DisplayName("The counter ability resolves after Luminarch Aspirant leaves the battlefield")
    void abilitySurvivesSourceRemoval() {
        Permanent aspirant = addCreatureReady(player1, new LuminarchAspirant());
        Permanent healer = addCreatureReady(player1, new ExpeditionHealer());

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, healer.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, aspirant));
        harness.passBothPriorities();

        assertThat(healer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not put a counter on a target that has left the battlefield")
    void removedTargetDoesNotReceiveCounter() {
        Permanent aspirant = addCreatureReady(player1, new LuminarchAspirant());
        Permanent healer = addCreatureReady(player1, new ExpeditionHealer());

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, healer.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, healer));
        harness.passBothPriorities();

        assertThat(healer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(aspirant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
