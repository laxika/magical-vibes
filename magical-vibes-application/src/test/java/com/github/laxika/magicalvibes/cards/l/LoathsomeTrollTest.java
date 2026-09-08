package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({LoathsomeTroll.class})
class LoathsomeTrollTest extends BaseCardTest {

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
    void lowRollPutsLoathsomeTrollOnTopOfLibrary() {
        LoathsomeTroll troll = activateTrollWithRoll(9);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(troll.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void middleRollReturnsLoathsomeTrollToHand() {
        LoathsomeTroll troll = activateTrollWithRoll(19);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).containsExactly(troll.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void naturalTwentyReturnsLoathsomeTrollTappedToBattlefield() {
        activateTrollWithRoll(20);

        Permanent troll = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getClass() == LoathsomeTroll.class)
                .findFirst()
                .orElseThrow();
        assertThat(troll.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private LoathsomeTroll activateTrollWithRoll(int result) {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(result));

        harness.setHand(player1, List.of());
        LoathsomeTroll troll = new LoathsomeTroll();
        harness.setGraveyard(player1, List.of(troll));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        return troll;
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
