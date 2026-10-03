package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
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

    @Test
    @DisplayName("The middle outcome puts the selected land onto the battlefield directly from the library")
    void middleOutcomeLandEntersFromLibrary() {
        resolveDruidTrigger(19);
        chooseBothLands();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof Plains)
                .singleElement()
                .extracting(Permanent::getEnteredFromZone)
                .isEqualTo(Zone.LIBRARY);
    }

    @Test
    @DisplayName("The controller can find just one land on the middle outcome")
    void middleOutcomeWithOneLand() {
        resolveDruidTrigger(19);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof Plains)
                .singleElement()
                .extracting(Permanent::isTapped)
                .isEqualTo(true);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Grizzly Bears");
    }

    @Test
    @DisplayName("Finding no lands still rolls the die and leaves the library intact")
    void findingNoLandsStillRolls() {
        resolveDruidTrigger(20);
        harness.handleCardChosen(player1, -1);

        assertThat(gameLogContains("rolls a d20 for Druid of the Emerald Grove: 20.")).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Plains", "Forest", "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Searching a library with no basic lands still counts as a library search")
    void noBasicLandsStillCountsAsSearch() {
        resolveDruidTrigger(9, new GrizzlyBears());

        assertThat(gd.playersWhoSearchedLibraryThisTurn).contains(player1.getId());
        assertThat(gameLogContains("rolls a d20 for Druid of the Emerald Grove: 9.")).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }

    private void resolveDruidTrigger(int roll) {
        resolveDruidTrigger(roll, new Plains(), new Forest(), new GrizzlyBears());
    }

    private void resolveDruidTrigger(int roll, Card... library) {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(roll));
        harness.setHand(player1, List.of(new DruidOfTheEmeraldGrove()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLibrary(player1, List.of(library));

        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    private void chooseBothLands() {
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
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
