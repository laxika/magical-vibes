package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.ArchersOfQarsi;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.j.JeskaiSage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HungeringYeti.class, ArchersOfQarsi.class, JeskaiSage.class, Forest.class})
class HungeringYetiTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast as though it had flash while controlling a green permanent")
    void canBeCastWithGreenPermanent() {
        harness.addToBattlefield(player1, new ArchersOfQarsi());
        prepareCastOnOpponentTurn();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Can be cast as though it had flash while controlling a blue permanent")
    void canBeCastWithBluePermanent() {
        harness.addToBattlefield(player1, new JeskaiSage());
        prepareCastOnOpponentTurn();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot be cast at instant timing without controlling a green or blue permanent")
    void cannotBeCastWithoutMatchingPermanent() {
        prepareCastOnOpponentTurn();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's green permanent does not grant flash")
    void opponentPermanentDoesNotGrantFlash() {
        harness.addToBattlefield(player2, new ArchersOfQarsi());
        prepareCastOnOpponentTurn();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be cast normally without a green or blue permanent")
    void canBeCastAtSorceryTimingWithoutMatchingPermanent() {
        prepareCastOnOpponentTurn();
        harness.forceActivePlayer(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hungering Yeti");
    }

    @Test
    @DisplayName("A Forest is colorless and does not grant instant timing")
    void forestDoesNotGrantFlash() {
        harness.addToBattlefield(player1, new Forest());
        prepareCastOnOpponentTurn();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A red permanent does not grant instant timing")
    void redPermanentDoesNotGrantFlash() {
        harness.addToBattlefield(player1, new HungeringYeti());
        prepareCastOnOpponentTurn();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Green cards in hand and graveyard do not grant instant timing")
    void greenCardsOutsideBattlefieldDoNotGrantFlash() {
        prepareCastOnOpponentTurn();
        harness.setHand(player1, List.of(new HungeringYeti(), new ArchersOfQarsi()));
        harness.setGraveyard(player1, List.of(new ArchersOfQarsi()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Losing the qualifying permanent before casting removes instant timing")
    void losingMatchingPermanentBeforeCastingRemovesFlash() {
        harness.addToBattlefield(player1, new ArchersOfQarsi());
        prepareCastOnOpponentTurn();
        gd.playerBattlefields.get(player1.getId()).clear();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Losing the qualifying permanent after casting does not prevent resolution")
    void losingMatchingPermanentAfterCastingDoesNotPreventResolution() {
        harness.addToBattlefield(player1, new ArchersOfQarsi());
        prepareCastOnOpponentTurn();

        harness.castCreature(player1, 0);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hungering Yeti");
    }

    private void prepareCastOnOpponentTurn() {
        harness.setHand(player1, List.of(new HungeringYeti()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
