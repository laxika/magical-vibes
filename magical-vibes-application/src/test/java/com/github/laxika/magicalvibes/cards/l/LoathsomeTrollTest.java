package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DragonsFire;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoathsomeTroll.class, DragonsFire.class})
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

    @Test
    void lowestRollPutsTrollAboveExistingLibraryCards() {
        LoathsomeTroll libraryCard = new LoathsomeTroll();
        harness.setLibrary(player1, List.of(libraryCard));

        LoathsomeTroll troll = activateTrollWithRoll(1);

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(troll.getId(), libraryCard.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void lowestMiddleRollReturnsTrollToHand() {
        LoathsomeTroll troll = activateTrollWithRoll(10);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactly(troll.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 20})
    void returnsOnlyTheActivatedCopy(int result) {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(result));
        harness.setHand(player1, List.of());
        LoathsomeTroll other = new LoathsomeTroll();
        LoathsomeTroll source = new LoathsomeTroll();
        harness.setGraveyard(player1, List.of(other, source));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactly(other.getId());
        if (result == 1) {
            assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(source.getId());
        } else if (result == 10) {
            assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                    .containsExactly(source.getId());
        } else {
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anySatisfy(permanent -> {
                        assertThat(permanent.getCard().getId()).isEqualTo(source.getId());
                        assertThat(permanent.isTapped()).isTrue();
                    });
        }
    }

    @Test
    void canActivateDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);

        LoathsomeTroll troll = activateTrollWithRoll(10);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactly(troll.getId());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 20})
    void olderActivationCannotReturnTrollAfterItLeavesAndReentersGraveyard(int result) {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(20));
        harness.setHand(player1, List.of());
        LoathsomeTroll troll = new LoathsomeTroll();
        harness.setGraveyard(player1, List.of(troll));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateGraveyardAbility(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Loathsome Troll");

        harness.setHand(player2, List.of(new DragonsFire()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Loathsome Troll"));
        harness.assertInGraveyard(player1, "Loathsome Troll");
        harness.assertNotOnBattlefield(player1, "Loathsome Troll");
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(result));

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactly(troll.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Loathsome Troll");
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
