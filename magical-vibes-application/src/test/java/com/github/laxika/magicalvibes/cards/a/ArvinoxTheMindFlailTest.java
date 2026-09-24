package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArvinoxTheMindFlail.class, Forest.class, GrizzlyBears.class, Shock.class})
class ArvinoxTheMindFlailTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of your end step, Arvinox exiles each opponent's bottom card face down")
    void exilesBottomCardOfEachOpponentFaceDown() {
        Card aboveBottom = new Shock();
        Card bottom = new GrizzlyBears();
        Permanent arvinox = addArvinoxWithBottomCard(aboveBottom, bottom);

        assertThat(gd.getCardsExiledByPermanent(arvinox.getId())).containsExactly(bottom);
        assertThat(gd.exiledCards).filteredOn(e -> arvinox.getId().equals(e.sourcePermanentId()))
                .allMatch(ExiledCardEntry::faceDown);
    }

    @Test
    @DisplayName("Controller may cast a nonland permanent exiled with Arvinox using any color of mana")
    void castsPermanentFromExileUsingAnyColor() {
        Card exiled = new GrizzlyBears();
        addArvinoxWithBottomCard(new Shock(), exiled);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castFromExile(player1, exiled.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Arvinox does not allow casting an exiled instant")
    void doesNotAllowCastingInstant() {
        Card instant = new Shock();
        addArvinoxWithBottomCard(new GrizzlyBears(), instant);
        assertThatThrownBy(() -> harness.castFromExile(player1, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Arvinox does not allow playing an exiled land")
    void doesNotAllowPlayingLand() {
        Card land = new Forest();
        addArvinoxWithBottomCard(new GrizzlyBears(), land);
        assertThatThrownBy(() -> harness.castFromExile(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addArvinoxWithBottomCard(Card aboveBottom, Card bottom) {
        Permanent arvinox = harness.addToBattlefieldAndReturn(player1, new ArvinoxTheMindFlail());
        harness.setLibrary(player2, List.of(aboveBottom, bottom));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        return arvinox;
    }
}
