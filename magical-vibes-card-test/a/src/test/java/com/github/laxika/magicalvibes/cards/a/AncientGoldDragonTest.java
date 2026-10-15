package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
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

@CardUsed(AncientGoldDragon.class)
class AncientGoldDragonTest extends BaseCardTest {

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
    @DisplayName("A combat-damage roll creates a number of Faerie Dragons equal to the result")
    void createsFaerieDragonsEqualToRollResult() {
        attackWithRoll(12);

        assertThat(countPermanents(player1, "Faerie Dragon")).isEqualTo(12);
    }

    @Test
    @DisplayName("A result of one creates one Faerie Dragon")
    void minimumRollCreatesOneFaerieDragon() {
        attackWithRoll(1);

        assertThat(countPermanents(player1, "Faerie Dragon")).isEqualTo(1);
    }

    @Test
    @DisplayName("A result of twenty creates twenty Faerie Dragons")
    void maximumRollCreatesTwentyFaerieDragons() {
        attackWithRoll(20);

        assertThat(countPermanents(player1, "Faerie Dragon")).isEqualTo(20);
    }

    @Test
    @DisplayName("Created tokens are untapped 1/1 blue Faerie Dragon creatures with flying")
    void createsTokensWithOracleCharacteristics() {
        attackWithRoll(3);

        assertThat(findPermanents(player1, "Faerie Dragon")).hasSize(3).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
            assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.FAERIE, CardSubtype.DRAGON);
            assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
            assertThat(token.isTapped()).isFalse();
        });
        assertThat(countPermanents(player2, "Faerie Dragon")).isZero();
    }

    @Test
    @DisplayName("The attacking Dragon's controller creates the tokens when player two attacks")
    void opponentControlledDragonCreatesTokensForOpponent() {
        attackWithRoll(player2, 9);

        assertThat(countPermanents(player2, "Faerie Dragon")).isEqualTo(9);
        assertThat(countPermanents(player1, "Faerie Dragon")).isZero();
    }

    @Test
    @DisplayName("Combat damage to a blocker does not roll a die or create tokens")
    void blockedDragonDoesNotCreateTokens() {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(12));
        addCreatureReady(player1, new AncientGoldDragon());
        addCreatureReady(player2, new AncientGoldDragon());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Faerie Dragon")).isZero();
        assertThat(countPermanents(player2, "Faerie Dragon")).isZero();
        assertThat(gameLogContains("rolls a d20")).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void attackWithRoll(int result) {
        attackWithRoll(player1, result);
    }

    private void attackWithRoll(Player controller, int result) {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(result));
        addCreatureReady(controller, new AncientGoldDragon());
        declareAttackers(controller, List.of(0));
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
