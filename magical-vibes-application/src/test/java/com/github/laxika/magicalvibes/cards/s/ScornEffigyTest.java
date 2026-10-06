package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScornEffigy.class})
class ScornEffigyTest extends BaseCardTest {

    @Test
    @DisplayName("Can be foretold and cast from exile on a later turn for no mana")
    void foretellsAndCastsOnLaterTurn() {
        ScornEffigy effigy = new ScornEffigy();
        harness.setHand(player1, List.of(effigy));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(effigy.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        gd.turnNumber++;
        harness.castFromExile(player1, effigy.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Scorn Effigy");
    }

    @Test
    void cannotForetellWithOnlyOneMana() {
        ScornEffigy effigy = new ScornEffigy();
        harness.setHand(player1, List.of(effigy));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Not enough mana to foretell");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(effigy);
        assertThat(gd.findExiledCard(effigy.getId())).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
    }

    @Test
    void cannotForetellDuringOpponentsTurn() {
        ScornEffigy effigy = new ScornEffigy();
        harness.setHand(player1, List.of(effigy));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Foretell can only be used during your turn");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(effigy);
        assertThat(gd.findExiledCard(effigy.getId())).isNull();
    }

    @Test
    void foretellIsASpecialActionOutsideMainPhase() {
        ScornEffigy effigy = new ScornEffigy();
        harness.setHand(player1, List.of(effigy));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.UPKEEP);

        harness.foretell(player1, 0);

        assertThat(gd.findExiledCard(effigy.getId()).faceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotCastOnTheTurnItWasForetold() {
        ScornEffigy effigy = new ScornEffigy();
        harness.setHand(player1, List.of(effigy));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.foretell(player1, 0);

        assertThatThrownBy(() -> harness.castFromExile(player1, effigy.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.findExiledCard(effigy.getId()).faceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void foretoldCreatureStillRequiresMainPhaseTiming() {
        ScornEffigy effigy = new ScornEffigy();
        harness.setHand(player1, List.of(effigy));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        gd.turnNumber++;
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castFromExile(player1, effigy.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot cast sorcery-speed spell from exile now");

        assertThat(gd.findExiledCard(effigy.getId()).faceDown()).isTrue();
        assertThat(gd.stack).isEmpty();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, effigy.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Scorn Effigy");
        assertThat(gd.findExiledCard(effigy.getId())).isNull();
    }
}
