package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.effect.normalfx.ArcaneEndeavorEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DiceRollService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArcaneEndeavor.class, Divination.class, Forest.class, GrizzlyBears.class, Opt.class})
class ArcaneEndeavorTest extends BaseCardTest {

    private ArcaneEndeavorEffectHandler effectHandler;
    private DiceRollService originalDiceRollService;

    @BeforeEach
    void captureDiceRollService() {
        effectHandler = GameTestEngineContext.get().getBean(ArcaneEndeavorEffectHandler.class);
        originalDiceRollService = (DiceRollService) ReflectionTestUtils.getField(effectHandler, "diceRollService");
    }

    @AfterEach
    void restoreDiceRollService() {
        ReflectionTestUtils.setField(effectHandler, "diceRollService", originalDiceRollService);
    }

    @Test
    void chosenResultDrawsThatManyAndOtherResultCapsInstantOrSorceryCast() {
        ArcaneEndeavor endeavor = new ArcaneEndeavor();
        Opt eligibleSpell = new Opt();
        Divination tooExpensiveSpell = new Divination();
        GrizzlyBears creature = new GrizzlyBears();
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(2, 1));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(endeavor, eligibleSpell, tooExpensiveSpell, creature));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "2");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5)
                .contains(eligibleSpell, tooExpensiveSpell, creature);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(eligibleSpell);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(eligibleSpell);
    }

    @Test
    void choosingTheOtherResultDrawsTheOtherNumber() {
        ArcaneEndeavor endeavor = new ArcaneEndeavor();
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(3, 6));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(endeavor));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "6");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        assertThat(gd.interaction.activeInteraction()).isNull();
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
