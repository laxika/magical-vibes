package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JohannApprenticeSorcerer.class, Shock.class, Pyroclasm.class, GrizzlyBears.class})
class JohannApprenticeSorcererTest extends BaseCardTest {

    @Test
    @DisplayName("Casts an instant from the top of the library for its normal cost")
    void castsInstantFromTopForNormalCost() {
        harness.addToBattlefield(player1, new JohannApprenticeSorcerer());
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        prepareMainPhase();

        harness.castAndResolveFromLibraryTop(player1, player2.getId());

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Casts a sorcery from the top of the library")
    void castsSorceryFromTop() {
        harness.addToBattlefield(player1, new JohannApprenticeSorcerer());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card pyroclasm = new Pyroclasm();
        harness.setLibrary(player1, List.of(pyroclasm));
        harness.addMana(player1, ManaColor.RED, 2);
        prepareMainPhase();

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Pyroclasm");
    }

    @Test
    @DisplayName("Does not cast a non-instant or non-sorcery card from the top")
    void rejectsNonInstantOrSorceryFromTop() {
        harness.addToBattlefield(player1, new JohannApprenticeSorcerer());
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        prepareMainPhase();

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(bears);
    }

    @Test
    @DisplayName("Allows only one top-library instant or sorcery cast each turn")
    void allowsOnlyOneTopLibraryCastEachTurn() {
        harness.addToBattlefield(player1, new JohannApprenticeSorcerer());
        Shock firstShock = new Shock();
        Shock secondShock = new Shock();
        harness.setLibrary(player1, List.of(firstShock, secondShock));
        harness.addMana(player1, ManaColor.RED, 2);
        prepareMainPhase();

        harness.castAndResolveFromLibraryTop(player1, player2.getId());

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(secondShock);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
