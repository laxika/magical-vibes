package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CommandTower;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PixieGuide;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.effect.normalfx.DiceRollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollTwoDiceChooseOneEffectHandler;
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

@CardUsed({WildEndeavor.class, PixieGuide.class, Forest.class, CommandTower.class})
class WildEndeavorTest extends BaseCardTest {

    private RollTwoDiceChooseOneEffectHandler effectHandler;
    private DiceRollService originalDiceRollService;

    @BeforeEach
    void captureDiceRollService() {
        effectHandler = GameTestEngineContext.get().getBean(RollTwoDiceChooseOneEffectHandler.class);
        originalDiceRollService = (DiceRollService) ReflectionTestUtils.getField(effectHandler, "diceRollService");
    }

    @AfterEach
    void restoreDiceRollService() {
        ReflectionTestUtils.setField(effectHandler, "diceRollService", originalDiceRollService);
    }

    @Test
    @DisplayName("Chosen roll creates beasts and the other roll searches tapped basic lands")
    void chosenRollCreatesBeastsAndTheOtherRollSearchesTappedBasicLands() {
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(3, 1));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        castWildEndeavor();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly("3", "1");

        harness.handleListChoice(player1, "3");
        assertThat(countPermanents(player1, "Beast")).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(
                permanent -> permanent.getCard().getName().equals("Forest"))
                .singleElement().satisfies(permanent -> assertThat(permanent.isTapped()).isTrue());
    }

    @Test
    @DisplayName("Choosing the other roll uses the first roll for land count")
    void choosingTheOtherRollUsesTheFirstRollForLandCount() {
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(1, 3));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        castWildEndeavor();

        harness.handleListChoice(player1, "1");
        assertThat(countPermanents(player1, "Beast")).isEqualTo(1);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(
                permanent -> permanent.getCard().getName().equals("Forest")).hasSize(3)
                .allSatisfy(permanent -> assertThat(permanent.isTapped()).isTrue());
    }

    @Test
    @DisplayName("Equal rolls resolve without a choice")
    void equalRollsResolveWithoutAChoice() {
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(2, 2));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        castWildEndeavor();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player1, "Beast")).isEqualTo(2);
    }

    @Test
    @DisplayName("Choosing second roll creates four beasts and finds one land")
    void choosingSecondRollCreatesFourBeastsAndFindsOneLand() {
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(1, 4));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        castWildEndeavor();

        harness.handleListChoice(player1, "4");
        harness.handleCardChosen(player1, 0);

        assertThat(countPermanents(player1, "Beast")).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Beast"))
                .hasSize(4).allSatisfy(permanent -> {
                    assertThat(permanent.getCard().isToken()).isTrue();
                    assertThat(permanent.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(permanent.getCard().getSubtypes()).contains(CardSubtype.BEAST);
                    assertThat(permanent.getCard().getPower()).isEqualTo(3);
                    assertThat(permanent.getCard().getToughness()).isEqualTo(3);
                });
        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Wild Endeavor");
    }

    @Test
    @DisplayName("Empty library still creates beasts and completes resolution")
    void emptyLibraryStillCreatesBeastsAndCompletesResolution() {
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(4, 4));
        harness.setLibrary(player1, List.of());
        castWildEndeavor();

        assertThat(countPermanents(player1, "Beast")).isEqualTo(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Wild Endeavor");
    }

    @Test
    @DisplayName("Fewer basic lands than rolled finds available lands and leaves nonlands")
    void fewerBasicLandsThanRolledFindsAvailableLandsAndLeavesNonlands() {
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(1, 4));
        WildEndeavor nonland = new WildEndeavor();
        harness.setLibrary(player1, List.of(nonland, new Forest(), new Forest()));
        castWildEndeavor();

        harness.handleListChoice(player1, "1");
        harness.handleCardChosen(player1, 0);
        assertThat(countPermanents(player1, "Forest")).isZero();
        harness.handleCardChosen(player1, 0);

        assertThat(countPermanents(player1, "Beast")).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Forest"))
                .hasSize(2).allSatisfy(permanent -> assertThat(permanent.isTapped()).isTrue());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Restricted search can stop after finding one basic land")
    void restrictedSearchCanStopAfterFindingOneBasicLand() {
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(1, 3));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        castWildEndeavor();

        harness.handleListChoice(player1, "1");
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(countPermanents(player1, "Beast")).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Forest"))
                .singleElement().satisfies(permanent -> assertThat(permanent.isTapped()).isTrue());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Nonbasic land cannot be found even though it is a land")
    void nonbasicLandCannotBeFoundEvenThoughItIsALand() {
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(2, 2));
        CommandTower nonbasic = new CommandTower();
        harness.setLibrary(player1, List.of(nonbasic));
        castWildEndeavor();

        assertThat(countPermanents(player1, "Beast")).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Command Tower");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonbasic);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Wild Endeavor");
    }

    private void castWildEndeavor() {
        harness.setHand(player1, List.of(new WildEndeavor()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Pixie Guide makes one roll of three d4 ignoring the lowest, not an extra die per d4")
    void pixieGuideAddsOneDieToTheWholeRoll() {
        harness.addToBattlefield(player1, new PixieGuide());
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(1, 2, 3, 4));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        castWildEndeavor();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("2", "3");
        harness.handleListChoice(player1, "3");

        assertThat(countPermanents(player1, "Beast")).isEqualTo(3);
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
