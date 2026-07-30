package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AdantoVanguard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GrandAbolisherTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent can't cast spells during the controller's turn")
    void opponentCantCastDuringControllersTurn() {
        harness.addToBattlefield(player1, new GrandAbolisher());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player2.getId())).isEmpty();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponent can cast spells during their own turn")
    void opponentCanCastOnOwnTurn() {
        harness.addToBattlefield(player1, new GrandAbolisher());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player2.getId())).contains(0);
    }

    @Test
    @DisplayName("Controller is not restricted by their own Grand Abolisher")
    void controllerCanCastOnOwnTurn() {
        harness.addToBattlefield(player1, new GrandAbolisher());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player1.getId())).contains(0);
    }

    @Test
    @DisplayName("Opponent can't activate a creature's ability during the controller's turn")
    void opponentCantActivateCreatureAbility() {
        harness.addToBattlefield(player1, new GrandAbolisher());
        Permanent vanguard = harness.addToBattlefieldAndReturn(player2, new AdantoVanguard());
        vanguard.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifacts, creatures");
    }

    @Test
    @DisplayName("Opponent can't tap a creature for mana during the controller's turn")
    void opponentCantTapManaCreature() {
        harness.addToBattlefield(player1, new GrandAbolisher());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        elves.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.tapPermanent(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifacts, creatures");
        assertThat(elves.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent can still tap lands for mana during the controller's turn")
    void opponentCanStillTapLands() {
        harness.addToBattlefield(player1, new GrandAbolisher());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.tapPermanent(player2, 0);

        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent can activate creature abilities on their own turn")
    void opponentCanActivateOnOwnTurn() {
        harness.addToBattlefield(player1, new GrandAbolisher());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        elves.setSummoningSick(false);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.tapPermanent(player2, 0);

        assertThat(elves.isTapped()).isTrue();
    }
}
