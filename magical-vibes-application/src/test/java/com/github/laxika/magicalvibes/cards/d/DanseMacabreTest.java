package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.effect.normalfx.D20RollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollD20EffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DanseMacabre.class, GrizzlyBears.class, HillGiant.class})
class DanseMacabreTest extends BaseCardTest {

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
    void lowResultReturnsOneChosenSacrifice() {
        Permanent casterChoice = addCreatureReady(player1, new GrizzlyBears());
        Permanent casterAlternative = addCreatureReady(player1, new HillGiant());
        Permanent opposingChoice = addCreatureReady(player2, new GrizzlyBears());
        setRoll(12);

        castDanseMacabre();

        PendingInteraction.MultiPermanentChoice sacrificeChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(sacrificeChoice.playerId()).isEqualTo(player1.getId());
        assertThat(sacrificeChoice.context()).isInstanceOf(MultiPermanentChoiceContext.DanseMacabreSacrifice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(casterChoice.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(casterAlternative.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(opposingChoice.getId()));

        PendingInteraction.MultiGraveyardChoice returnChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(returnChoice.minCount()).isEqualTo(1);
        assertThat(returnChoice.maxCount()).isEqualTo(1);
        assertThat(returnChoice.validCardIds())
                .containsExactlyInAnyOrder(casterChoice.getCard().getId(), opposingChoice.getCard().getId());

        harness.handleMultipleCardsChosen(player1, List.of(opposingChoice.getCard().getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(opposingChoice.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(casterChoice.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(opposingChoice.getCard().getId()));
    }

    @Test
    void highResultReturnsUpToTwoSacrificesUnderCasterControl() {
        Card casterCreature = new GrizzlyBears();
        Card opposingCreature = new HillGiant();
        addCreatureReady(player1, casterCreature);
        addCreatureReady(player2, opposingCreature);
        setRoll(13);

        castDanseMacabre();

        PendingInteraction.MultiGraveyardChoice returnChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(returnChoice.minCount()).isZero();
        assertThat(returnChoice.maxCount()).isEqualTo(2);
        harness.handleMultipleCardsChosen(player1, List.of(casterCreature.getId(), opposingCreature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(casterCreature.getId(), opposingCreature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(casterCreature.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(opposingCreature.getId()));
    }

    private void castDanseMacabre() {
        harness.setHand(player1, List.of(new DanseMacabre()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0);
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
