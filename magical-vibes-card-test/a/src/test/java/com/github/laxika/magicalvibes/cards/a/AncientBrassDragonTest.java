package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.effect.normalfx.D20RollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollD20EffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AncientBrassDragon.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class AncientBrassDragonTest extends BaseCardTest {

    private RollD20EffectHandler rollD20EffectHandler;
    private D20RollService originalD20RollService;

    @BeforeEach
    void captureD20RollService() {
        rollD20EffectHandler = GameTestEngineContext.get().getBean(RollD20EffectHandler.class);
        originalD20RollService = (D20RollService) ReflectionTestUtils.getField(
                rollD20EffectHandler, "d20RollService");
    }

    @AfterEach
    void restoreD20RollService() {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", originalD20RollService);
    }

    @Test
    @DisplayName("The roll returns target creature cards from any graveyard under the dragon's control")
    void returnsCreatureCardsFromAnyGraveyardUnderControllerControl() {
        Card ownCreature = new GrizzlyBears();
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));

        triggerWithRoll(5);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownCreature.getId(), opponentCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId(), opponentCreature.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(ownCreature.getId(), opponentCreature.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .doesNotContain(opponentCreature.getId());
    }

    @Test
    @DisplayName("The roll's result is the aggregate mana-value limit")
    void enforcesAggregateManaValueLimit() {
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstCreature, secondCreature));

        triggerWithRoll(3);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(firstCreature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total mana value");

        harness.handleMultipleCardsChosen(player1, List.of(firstCreature.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(firstCreature.getId())
                .doesNotContain(secondCreature.getId());
    }

    @Test
    @DisplayName("Only creature cards within the roll's limit are legal targets")
    void filtersNoncreaturesAndOverLimitCreatures() {
        Card creature = new GrizzlyBears();
        Card tooExpensive = new HillGiant();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(creature, tooExpensive, land));

        triggerWithRoll(2);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(creature.getId())
                .doesNotContain(tooExpensive.getId(), land.getId());
    }

    @Test
    @DisplayName("The controller may choose no creatures even when legal targets exist")
    void mayChooseNoTargets() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));

        triggerWithRoll(5);
        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .doesNotContain(creature.getId());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Empty graveyards do not prevent the die roll or leave a target prompt")
    void rollsWithEmptyGraveyards() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());

        triggerWithRoll(10);

        assertThat(gameLogContains("rolls a d20 for Ancient Brass Dragon: 10.")).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A roll of one cannot return a creature with mana value two")
    void rollOfOneLeavesOverLimitCreatureInGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        triggerWithRoll(1);

        assertThat(gameLogContains("rolls a d20 for Ancient Brass Dragon: 1.")).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A natural twenty returns creatures whose total mana value is exactly twenty")
    void naturalTwentyReturnsCreaturesAtExactAggregateLimit() {
        Card firstDragon = new AncientBrassDragon();
        Card secondDragon = new AncientBrassDragon();
        Card giant = new HillGiant();
        Card bear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstDragon, giant));
        harness.setGraveyard(player2, List.of(secondDragon, bear));

        triggerWithRoll(20);
        harness.handleMultipleCardsChosen(player1,
                List.of(firstDragon.getId(), secondDragon.getId(), giant.getId(), bear.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(firstDragon.getId(), secondDragon.getId(), giant.getId(), bear.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
    @Test
    @DisplayName("The reflexive ability resolves after the dragon leaves the battlefield")
    void returnsCreaturesAfterSourceLeavesBattlefield() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));

        triggerWithRoll(2);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(creature.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A remaining legal target returns when another target leaves its graveyard")
    void returnsRemainingLegalTarget() {
        Card removedCreature = new GrizzlyBears();
        Card remainingCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(removedCreature));
        harness.setGraveyard(player2, List.of(remainingCreature));

        triggerWithRoll(4);
        harness.withAutoStop(gd.currentStep, () -> harness.handleMultipleCardsChosen(player1,
                List.of(removedCreature.getId(), remainingCreature.getId())));
        assertThat(gd.stack).isNotEmpty();
        harness.setGraveyard(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(remainingCreature.getId())
                .doesNotContain(removedCreature.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
    private void triggerWithRoll(int result) {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(result));
        addCreatureReady(player1, new AncientBrassDragon());
        declareAttackers(List.of(0));
        resolveAllTriggers();
    }

    private static final class FixedD20RollService extends D20RollService {

        private final int result;

        private FixedD20RollService(int result) {
            this.result = result;
        }

        @Override
        public int roll() {
            return result;
        }
    }
}
