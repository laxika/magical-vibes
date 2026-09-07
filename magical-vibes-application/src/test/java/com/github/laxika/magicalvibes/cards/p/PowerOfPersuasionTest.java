package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
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

@CardUsed({PowerOfPersuasion.class, GrizzlyBears.class, Island.class})
class PowerOfPersuasionTest extends BaseCardTest {

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
    @DisplayName("A result of 9 returns the target to its owner's hand")
    void nineReturnsTargetToHand() {
        setRoll(9);
        Permanent target = addTarget();

        castPowerOfPersuasion(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Power of Persuasion");
    }

    @Test
    @DisplayName("A result of 10 lets the target's owner choose the library destination")
    void tenPutsTargetOnChosenLibraryDestination() {
        setRoll(10);
        Permanent target = addTarget();
        Card topCard = new Island();
        Card nextCard = new Island();
        harness.setLibrary(player2, List.of(topCard, nextCard));

        castPowerOfPersuasion(target);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.TargetLibraryDestinationChoice.class)
                .playerId()).isEqualTo(player2.getId());

        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, nextCard, target.getCard());
    }

    @Test
    @DisplayName("A result of 20 gives control through the caster's next turn")
    void twentyGivesControlThroughNextTurn() {
        setRoll(20);
        Permanent target = addTarget();

        castPowerOfPersuasion(target);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);

        passToCleanup(player2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);

        passToCleanup(player1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Cannot target a creature controlled by its caster")
    void cannotTargetOwnCreature() {
        setRoll(20);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PowerOfPersuasion()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addTarget() {
        return harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
    }

    private void castPowerOfPersuasion(Permanent target) {
        harness.setHand(player1, List.of(new PowerOfPersuasion()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void passToCleanup(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void setRoll(int result) {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(result));
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
