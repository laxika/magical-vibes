package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.effect.normalfx.DiceRollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ValiantEndeavorEffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ValiantEndeavor.class, AirElemental.class, GrizzlyBears.class})
class ValiantEndeavorTest extends BaseCardTest {

    private ValiantEndeavorEffectHandler effectHandler;
    private DiceRollService originalDiceRollService;

    @BeforeEach
    void captureDiceRollService() {
        effectHandler = GameTestEngineContext.get().getBean(ValiantEndeavorEffectHandler.class);
        originalDiceRollService = (DiceRollService) ReflectionTestUtils.getField(effectHandler, "diceRollService");
    }

    @AfterEach
    void restoreDiceRollService() {
        ReflectionTestUtils.setField(effectHandler, "diceRollService", originalDiceRollService);
    }

    @Test
    void choosingFirstRollDestroysCreaturesAtLeastThatPowerAndCreatesTheOtherNumberOfKnights() {
        castWithRolls(4, 2);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly("4", "2");

        harness.handleListChoice(player1, "4");

        assertThat(countPermanents(player1, "Knight")).isEqualTo(2);
        assertThat(countPermanents(player2, "Air Elemental")).isZero();
        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    void choosingSecondRollUsesTheFirstRollForKnightCount() {
        castWithRolls(2, 4);

        harness.handleListChoice(player1, "4");

        assertThat(countPermanents(player1, "Knight")).isEqualTo(2);
        assertThat(countPermanents(player2, "Air Elemental")).isZero();
        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    void equalRollsResolveWithoutAChoice() {
        castWithRolls(3, 3);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player1, "Knight")).isEqualTo(3);
        assertThat(countPermanents(player2, "Air Elemental")).isZero();
        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(1);
    }

    private void castWithRolls(int first, int second) {
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(first, second));
        harness.addToBattlefield(player2, new AirElemental());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ValiantEndeavor()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
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
