package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindlockOrb;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

@CardUsed({TreasureChest.class, GrizzlyBears.class, MindlockOrb.class, PsychogenicProbe.class})
class TreasureChestTest extends BaseCardTest {

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
    void naturalOneMakesItsControllerLoseThreeLife() {
        int startingLife = gd.getLife(player1.getId());
        activate(1, List.of());

        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife - 3);
    }

    @Test
    void lowRollCreatesFiveTreasureTokens() {
        activate(9, List.of());

        assertThat(findPermanents(player1, "Treasure")).hasSize(5);
    }

    @Test
    void middleRollGainsThreeLifeAndDrawsThreeCards() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        int startingLife = gd.getLife(player1.getId());
        activate(10, List.of(first, second, third));

        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife + 3);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
    }

    @Test
    void naturalTwentyCanPutTheSearchedArtifactOntoTheBattlefield() {
        Card searched = new TreasureChest();
        activate(20, List.of(searched));
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Treasure Chest")).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(searched);
    }

    @Test
    void naturalTwentyPutsAChosenNonartifactIntoItsControllersHand() {
        Card searched = new GrizzlyBears();
        activate(20, List.of(searched));
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(searched);
    }

    @Test
    void naturalTwentyMayPutAnArtifactIntoItsControllersHandInstead() {
        Card searched = new TreasureChest();
        activate(20, List.of(searched));
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Treasure Chest")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(searched);
    }

    @Test
    void lowestTreasureRollCreatesFiveTokens() {
        activate(2, List.of());

        assertThat(findPermanents(player1, "Treasure")).hasSize(5);
        assertThat(findPermanents(player1, "Treasure Chest")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof TreasureChest);
    }

    @Test
    void highestMiddleRollGainsLifeAndDrawsThreeCards() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        int startingLife = gd.getLife(player1.getId());
        activate(19, List.of(first, second, third));

        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife + 3);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
    }

    @Test
    void naturalTwentyCanFindAnArtifactBelowTheTopCard() {
        Card remaining = new GrizzlyBears();
        Card searched = new TreasureChest();
        activate(20, List.of(remaining, searched));
        harness.handleCardChosen(player1, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Treasure Chest")).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void naturalTwentyWithAnEmptyLibraryDoesNotOfferAnArtifactChoice() {
        activate(20, List.of());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Treasure Chest")).isEmpty();
    }

    @Test
    void sacrificeIsPaidBeforeTheAbilityResolves() {
        setRoll(1);
        harness.addToBattlefield(player1, new TreasureChest());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int startingLife = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);

        assertThat(findPermanents(player1, "Treasure Chest")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof TreasureChest);
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife);

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife - 3);
    }

    @Test
    void searchedArtifactEntersBeforeTheLibraryIsShuffled() {
        Card searched = new PsychogenicProbe();
        int startingLife = gd.getLife(player1.getId());
        activate(20, List.of(searched));
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Psychogenic Probe")).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife - 2);
    }

    @Test
    void prohibitedSearchDoesNotPutAnUnsearchedCardIntoHand() {
        Card remaining = new GrizzlyBears();
        harness.addToBattlefield(player2, new MindlockOrb());
        activate(20, List.of(remaining));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void activate(int roll, List<Card> library) {
        setRoll(roll);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, library);
        harness.addToBattlefield(player1, new TreasureChest());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
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
