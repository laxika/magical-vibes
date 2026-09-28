package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

@CardUsed({SongOfInspiration.class, AirElemental.class, GrizzlyBears.class, Shock.class})
class SongOfInspirationTest extends BaseCardTest {

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
    void returnsUpToTwoPermanentCardsAndExcludesNonpermanents() {
        Card creature = new GrizzlyBears();
        Card permanent = new AirElemental();
        Card instant = new Shock();
        harness.setGraveyard(player1, List.of(creature, permanent, instant));
        harness.setHand(player1, List.of(new SongOfInspiration()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        setRoll(1);

        harness.castInstant(player1, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId(), permanent.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), permanent.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(creature, permanent);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(instant);
    }

    @Test
    void highResultGainsLifeEqualToSelectedCardsTotalManaValue() {
        Card first = new GrizzlyBears();
        Card second = new AirElemental();
        Card unselected = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second, unselected));
        harness.setHand(player1, List.of(new SongOfInspiration()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player1, 10);
        setRoll(8);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId())).contains(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(unselected);
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
