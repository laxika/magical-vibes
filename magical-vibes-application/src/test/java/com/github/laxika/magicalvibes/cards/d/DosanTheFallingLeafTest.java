package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EtherealHaze;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KabutoMoth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DosanTheFallingLeaf.class, EtherealHaze.class, Forest.class, KabutoMoth.class})
class DosanTheFallingLeafTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent can't cast spells during the controller's turn")
    void opponentCantCastDuringControllersTurn() {
        harness.addToBattlefield(player1, new DosanTheFallingLeaf());
        harness.setHand(player2, List.of(new EtherealHaze()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passPriority(player1);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player2.getId())).isEmpty();

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Controller can't cast spells during the opponent's turn")
    void controllerCantCastOnOpponentsTurn() {
        harness.addToBattlefield(player1, new DosanTheFallingLeaf());
        harness.setHand(player1, List.of(new EtherealHaze()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passPriority(player2);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player1.getId())).isEmpty();

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponent can cast spells during their own turn")
    void opponentCanCastOnOwnTurn() {
        harness.addToBattlefield(player1, new DosanTheFallingLeaf());
        harness.setHand(player2, List.of(new EtherealHaze()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player2.getId())).contains(0);
        harness.castInstant(player2, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Controller can cast spells during their own turn")
    void controllerCanCastOnOwnTurn() {
        harness.addToBattlefield(player1, new DosanTheFallingLeaf());
        harness.setHand(player1, List.of(new EtherealHaze()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player1.getId())).contains(0);
        harness.castInstant(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Losing Dosan's abilities removes its spell restriction")
    void losingAllAbilitiesRemovesRestriction() {
        Permanent dosan = harness.addToBattlefieldAndReturn(player1, new DosanTheFallingLeaf());
        dosan.setLosesAllAbilitiesUntilEndOfTurn(true);
        harness.setHand(player2, List.of(new EtherealHaze()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passPriority(player1);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player2.getId())).contains(0);
        harness.castInstant(player2, 0);
    }

    @Test
    @DisplayName("Opponent can still tap lands for mana during the controller's turn")
    void opponentCanStillTapLands() {
        harness.addToBattlefield(player1, new DosanTheFallingLeaf());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.tapPermanent(player2, 0);

        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent can still activate non-mana abilities during the controller's turn")
    void opponentCanStillActivateAbilities() {
        Permanent dosan = harness.addToBattlefieldAndReturn(player1, new DosanTheFallingLeaf());
        addCreatureReady(player2, new KabutoMoth());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player2, 0, null, dosan.getId());
        harness.passBothPriorities();

        assertThat(dosan.getPowerModifier()).isEqualTo(1);
        assertThat(dosan.getToughnessModifier()).isEqualTo(2);
    }
}
