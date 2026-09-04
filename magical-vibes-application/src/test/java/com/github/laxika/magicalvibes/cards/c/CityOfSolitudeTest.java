package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CityOfSolitude.class, Chronatog.class, Forest.class, LlanowarElves.class})
class CityOfSolitudeTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent can't cast spells during the controller's turn")
    void opponentCantCastDuringControllersTurn() {
        harness.addToBattlefield(player1, new CityOfSolitude());
        harness.setHand(player2, List.of(new CityOfSolitude()));
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player2.getId())).isEmpty();

        assertThatThrownBy(() -> harness.castEnchantment(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponent can cast spells during their own turn")
    void opponentCanCastOnOwnTurn() {
        harness.addToBattlefield(player1, new CityOfSolitude());
        harness.setHand(player2, List.of(new CityOfSolitude()));
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player2.getId())).contains(0);
    }

    @Test
    @DisplayName("Controller can't cast spells during opponent's turn")
    void controllerCantCastOnOpponentsTurn() {
        harness.addToBattlefield(player1, new CityOfSolitude());
        harness.setHand(player1, List.of(new CityOfSolitude()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player1.getId())).isEmpty();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Controller can cast spells during their own turn")
    void controllerCanCastOnOwnTurn() {
        harness.addToBattlefield(player1, new CityOfSolitude());
        harness.setHand(player1, List.of(new CityOfSolitude()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player1.getId())).contains(0);
    }

    @Test
    @DisplayName("Losing City of Solitude's abilities removes its restriction")
    void losingAllAbilitiesRemovesRestriction() {
        Permanent city = harness.addToBattlefieldAndReturn(player1, new CityOfSolitude());
        city.setLosesAllAbilitiesUntilEndOfTurn(true);
        harness.setHand(player2, List.of(new CityOfSolitude()));
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player2.getId())).contains(0);
        harness.castEnchantment(player2, 0);
    }

    @Test
    @DisplayName("A face-down City of Solitude does not restrict actions")
    void faceDownCityDoesNotRestrictActions() {
        Permanent city = harness.addToBattlefieldAndReturn(player1, new CityOfSolitude());
        city.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.setHand(player2, List.of(new CityOfSolitude()));
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player2.getId())).contains(0);
        harness.castEnchantment(player2, 0);
    }

    @Test
    @DisplayName("Opponent can't activate a creature ability during the controller's turn")
    void opponentCantActivateCreatureAbility() {
        harness.addToBattlefield(player1, new CityOfSolitude());
        addCreatureReady(player2, new Chronatog());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("own turn");
    }

    @Test
    @DisplayName("Opponent can't tap lands for mana during the controller's turn")
    void opponentCantTapLandsForMana() {
        harness.addToBattlefield(player1, new CityOfSolitude());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.tapPermanent(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("own turn");
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent can't tap a mana creature during the controller's turn")
    void opponentCantTapManaCreature() {
        harness.addToBattlefield(player1, new CityOfSolitude());
        Permanent elves = addCreatureReady(player2, new LlanowarElves());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.tapPermanent(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("own turn");
        assertThat(elves.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent can activate abilities on their own turn")
    void opponentCanActivateOnOwnTurn() {
        harness.addToBattlefield(player1, new CityOfSolitude());
        Permanent elves = addCreatureReady(player2, new LlanowarElves());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.tapPermanent(player2, 0);

        assertThat(elves.isTapped()).isTrue();
    }
}
