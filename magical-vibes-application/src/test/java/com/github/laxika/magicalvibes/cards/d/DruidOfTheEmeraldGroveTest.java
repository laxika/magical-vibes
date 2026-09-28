package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DruidOfTheEmeraldGrove.class, Forest.class, GrizzlyBears.class, Plains.class})
class DruidOfTheEmeraldGroveTest extends BaseCardTest {

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
    @DisplayName("A result of 9 puts the revealed basic lands into its controller's hand")
    void lowRollPutsLandsIntoHand() {
        resolveDruidTrigger(9);
        chooseBothLands();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Plains", "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().hasType(com.github.laxika.magicalvibes.model.CardType.LAND));
    }

    @Test
    @DisplayName("A result of 20 puts both revealed basic lands onto the battlefield tapped")
    void maximumRollPutsBothLandsOntoBattlefieldTapped() {
        resolveDruidTrigger(20);
        chooseBothLands();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(com.github.laxika.magicalvibes.model.CardType.LAND)))
                .hasSize(2)
                .allMatch(Permanent::isTapped);
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.hasType(com.github.laxika.magicalvibes.model.CardType.LAND));
    }

    @Test
    @DisplayName("A result from 10 through 19 puts one revealed land onto the battlefield and one into hand")
    void middleRollSplitsTheLands() {
        resolveDruidTrigger(10);
        chooseBothLands();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(
                        com.github.laxika.magicalvibes.model.CardType.LAND))
                .singleElement()
                .extracting(Permanent::isTapped)
                .isEqualTo(true);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest");
    }

    private void resolveDruidTrigger(int roll) {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(roll));
        harness.setHand(player1, List.of(new DruidOfTheEmeraldGrove()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        setLibrary(new Plains(), new Forest(), new GrizzlyBears());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void chooseBothLands() {
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
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
