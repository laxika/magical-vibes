package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.effect.normalfx.DiceRollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.GraveEndeavorEffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GraveEndeavor.class, Forest.class, GrizzlyBears.class})
class GraveEndeavorTest extends BaseCardTest {

    private GraveEndeavorEffectHandler effectHandler;
    private DiceRollService originalDiceRollService;

    @BeforeEach
    void captureDiceRollService() {
        effectHandler = GameTestEngineContext.get().getBean(GraveEndeavorEffectHandler.class);
        originalDiceRollService = (DiceRollService) ReflectionTestUtils.getField(effectHandler, "diceRollService");
    }

    @AfterEach
    void restoreDiceRollService() {
        ReflectionTestUtils.setField(effectHandler, "diceRollService", originalDiceRollService);
    }

    @Test
    void chosenRollSetsCountersAndOtherRollDrains() {
        GraveEndeavor endeavor = new GraveEndeavor();
        Card invalidCard = new Forest();
        GrizzlyBears creature = new GrizzlyBears();
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(4, 2));
        harness.setGraveyard(player1, List.of(invalidCard, creature));
        harness.castFromHand(player1, endeavor, "{5}{B}{B}");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("4", "2");

        harness.handleListChoice(player1, "4");
        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice.validIndices()).containsExactly(1);
        harness.handleGraveyardCardChosen(player1, 1);

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst().orElseThrow();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void equalRollsNeedNoNumberChoiceAndUseTheSameValueForBothEffects() {
        GraveEndeavor endeavor = new GraveEndeavor();
        GrizzlyBears creature = new GrizzlyBears();
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(3, 3));
        harness.setGraveyard(player1, List.of(creature));
        harness.castFromHand(player1, endeavor, "{5}{B}{B}");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNotNull();
        harness.handleGraveyardCardChosen(player1, 0);

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst().orElseThrow();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    void returningAvailableCreatureCannotBeDeclined() {
        GrizzlyBears creature = new GrizzlyBears();
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(3, 3));
        harness.setGraveyard(player1, List.of(creature));
        harness.castFromHand(player1, new GraveEndeavor(), "{5}{B}{B}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);

        harness.handleGraveyardCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    void choosingLowerSecondRollUsesHigherFirstRollForLifeChanges() {
        GrizzlyBears creature = new GrizzlyBears();
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(10, 1));
        harness.setGraveyard(player1, List.of(creature));
        harness.castFromHand(player1, new GraveEndeavor(), "{5}{B}{B}");
        harness.passBothPriorities();

        harness.handleListChoice(player1, "1");
        harness.handleGraveyardCardChosen(player1, 0);

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst().orElseThrow();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 30);
        harness.assertLife(player2, 10);
    }

    @Test
    void emptyGraveyardStillDrainsUsingUnchosenRoll() {
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(2, 7));
        harness.setGraveyard(player1, List.of());
        harness.castFromHand(player1, new GraveEndeavor(), "{5}{B}{B}");
        harness.passBothPriorities();

        harness.handleListChoice(player1, "2");

        harness.assertLife(player1, 27);
        harness.assertLife(player2, 13);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void opponentsCreatureCannotBeReturnedWhenOwnGraveyardHasOnlyNoncreatures() {
        GrizzlyBears opposingCreature = new GrizzlyBears();
        Forest land = new Forest();
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(5, 5));
        harness.setGraveyard(player1, List.of(land));
        harness.setGraveyard(player2, List.of(opposingCreature));
        harness.castFromHand(player1, new GraveEndeavor(), "{5}{B}{B}");
        harness.passBothPriorities();

        harness.assertLife(player1, 25);
        harness.assertLife(player2, 15);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    private static final class FixedDiceRollService extends DiceRollService {

        private final int[] results;
        private int index;

        private FixedDiceRollService(int... results) {
            this.results = results;
        }

        @Override
        public int roll(int sides) {
            return results[Math.min(index++, results.length - 1)];
        }
    }
}
