package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CommandTower;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.effect.normalfx.D4RollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.WildEndeavorEffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WildEndeavor.class, Forest.class, CommandTower.class})
class WildEndeavorTest extends BaseCardTest {

    private WildEndeavorEffectHandler effectHandler;
    private D4RollService originalD4RollService;

    @BeforeEach
    void captureD4RollService() {
        effectHandler = GameTestEngineContext.get().getBean(WildEndeavorEffectHandler.class);
        originalD4RollService = (D4RollService) ReflectionTestUtils.getField(effectHandler, "d4RollService");
    }

    @AfterEach
    void restoreD4RollService() {
        ReflectionTestUtils.setField(effectHandler, "d4RollService", originalD4RollService);
    }

    @Test
    void chosenRollCreatesBeastsAndTheOtherRollSearchesTappedBasicLands() {
        ReflectionTestUtils.setField(effectHandler, "d4RollService", new FixedD4RollService(3, 1));
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
    void choosingTheOtherRollUsesTheFirstRollForLandCount() {
        ReflectionTestUtils.setField(effectHandler, "d4RollService", new FixedD4RollService(1, 3));
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
    void equalRollsResolveWithoutAChoice() {
        ReflectionTestUtils.setField(effectHandler, "d4RollService", new FixedD4RollService(2, 2));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        castWildEndeavor();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player1, "Beast")).isEqualTo(2);
    }

    @Test
    void choosingSecondRollCreatesFourBeastsAndFindsOneLand() {
        ReflectionTestUtils.setField(effectHandler, "d4RollService", new FixedD4RollService(1, 4));
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
    void emptyLibraryStillCreatesBeastsAndCompletesResolution() {
        ReflectionTestUtils.setField(effectHandler, "d4RollService", new FixedD4RollService(4, 4));
        harness.setLibrary(player1, List.of());
        castWildEndeavor();

        assertThat(countPermanents(player1, "Beast")).isEqualTo(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Wild Endeavor");
    }

    @Test
    void fewerBasicLandsThanRolledFindsAvailableLandsAndLeavesNonlands() {
        ReflectionTestUtils.setField(effectHandler, "d4RollService", new FixedD4RollService(1, 4));
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
    void restrictedSearchCanStopAfterFindingOneBasicLand() {
        ReflectionTestUtils.setField(effectHandler, "d4RollService", new FixedD4RollService(1, 3));
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
    void nonbasicLandCannotBeFoundEvenThoughItIsALand() {
        ReflectionTestUtils.setField(effectHandler, "d4RollService", new FixedD4RollService(2, 2));
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

    private static final class FixedD4RollService extends D4RollService {

        private final int[] results;
        private int index;

        private FixedD4RollService(int... results) {
            this.results = results;
        }

        @Override
        public int roll() {
            return results[Math.min(index++, results.length - 1)];
        }
    }
}
