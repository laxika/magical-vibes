package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({MathiseSurgeChanneler.class, Divination.class, GrizzlyBears.class, LightningBolt.class})
class MathiseSurgeChannelerTest extends BaseCardTest {

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
    @DisplayName("A low roll makes each player draw a card")
    void lowRollEachPlayerDraws() {
        castDivinationWithRoll(9);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(7);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(9);
    }

    @Test
    @DisplayName("A middle roll makes only Mathise's controller draw a card")
    void middleRollControllerDraws() {
        castDivinationWithRoll(10);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(7);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(10);
    }

    @Test
    @DisplayName("A result of 20 copies the qualifying spell")
    void maximumRollCopiesSpell() {
        castDivinationWithRoll(20);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(10);
    }

    @Test
    @DisplayName("Spells below mana value 3 do not trigger Mathise")
    void lowManaValueSpellDoesNotTrigger() {
        setRoll(20);
        harness.addToBattlefield(player1, new MathiseSurgeChanneler());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() instanceof MathiseSurgeChanneler);
    }

    private void castDivinationWithRoll(int roll) {
        setRoll(roll);
        harness.addToBattlefield(player1, new MathiseSurgeChanneler());
        harness.setHand(player1, List.of(new Divination()));
        harness.setLibrary(player1, library(10));
        harness.setLibrary(player2, library(10));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();
    }

    private List<Card> library(int size) {
        return java.util.stream.IntStream.range(0, size)
                .mapToObj(ignored -> (Card) new GrizzlyBears())
                .toList();
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
