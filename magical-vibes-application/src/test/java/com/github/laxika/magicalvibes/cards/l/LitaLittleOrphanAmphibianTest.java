package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LitaLittleOrphanAmphibian.class, GrizzlyBears.class})
class LitaLittleOrphanAmphibianTest extends BaseCardTest {

    private static final String COUNTER = "Put a +1/+1 counter on Lita";
    private static final String FOOD = "Create a Food token";
    private static final String SCRY = "Scry 1";

    @Test
    @DisplayName("Alliance can put a +1/+1 counter on Lita")
    void alliancePutsCounterOnLita() {
        Permanent lita = addLita();

        castGrizzlyBears();
        harness.handleListChoice(player1, COUNTER);
        harness.passBothPriorities();

        assertThat(lita.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Alliance can create a Food token")
    void allianceCreatesFood() {
        addLita();

        castGrizzlyBears();
        harness.handleListChoice(player1, FOOD);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Alliance can scry 1")
    void allianceScriesOne() {
        addLita();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop = deck.getFirst();

        castGrizzlyBears();
        harness.handleListChoice(player1, SCRY);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(deck.getLast()).isSameAs(originalTop);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Alliance choices are limited to one of each choice per turn")
    void allianceChoiceCannotBeChosenAgainThisTurn() {
        addLita();

        castGrizzlyBears();
        harness.handleListChoice(player1, COUNTER);
        harness.passBothPriorities();

        castGrizzlyBears();

        assertThatThrownBy(() -> harness.handleListChoice(player1, COUNTER))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, FOOD);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Food"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Lita does not trigger for a creature entering under an opponent's control")
    void allianceDoesNotTriggerForOpponentCreature() {
        addLita();

        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addLita() {
        return harness.enterBattlefieldAndReturn(player1, new LitaLittleOrphanAmphibian());
    }

    @Test
    void ownEntryDoesNotTriggerAlliance() {
        addLita();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void choicesBecomeAvailableAgainNextTurn() {
        Permanent lita = addLita();
        castGrizzlyBears();
        harness.handleListChoice(player1, COUNTER);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.handleListChoice(player1, COUNTER);
        harness.passBothPriorities();

        assertThat(lita.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void noFurtherAbilityIsStackedAfterAllModesAreChosen() {
        Permanent lita = addLita();
        castGrizzlyBears();
        harness.handleListChoice(player1, COUNTER);
        harness.passBothPriorities();
        castGrizzlyBears();
        harness.handleListChoice(player1, FOOD);
        harness.passBothPriorities();
        castGrizzlyBears();
        harness.handleListChoice(player1, SCRY);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(lita.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void choiceIsConsumedBeforeTheAbilityResolves() {
        Permanent lita = addLita();
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.handleListChoice(player1, COUNTER);

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThatThrownBy(() -> harness.handleListChoice(player1, COUNTER))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, FOOD);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(lita.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    void foodCanBeSacrificedForThreeLifeImmediately() {
        addLita();
        castGrizzlyBears();
        harness.handleListChoice(player1, FOOD);
        harness.passBothPriorities();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        Permanent food = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Food"))
                .findFirst().orElseThrow();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(food), null, null);
        harness.assertNotOnBattlefield(player1, "Food");
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 3);
    }

    private void castGrizzlyBears() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
