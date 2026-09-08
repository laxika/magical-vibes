package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AltanakTheThriceCalled;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SayItsName.class, AltanakTheThriceCalled.class, Forest.class, GrizzlyBears.class})
class SayItsNameTest extends BaseCardTest {

    @Test
    @DisplayName("Mills three cards before offering the creature or land return")
    void millsThenOffersReturn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SayItsName()));
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the may returns a milled land to hand")
    void acceptsReturnOfMilledLand() {
        Forest land = new Forest();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SayItsName()));
        harness.setLibrary(player1, List.of(land, new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("The graveyard ability exiles itself and two named cards to return Altanak")
    void exilesNamedCardsAndReturnsAltanak() {
        SayItsName source = new SayItsName();
        SayItsName other = new SayItsName();
        SayItsName third = new SayItsName();
        AltanakTheThriceCalled altanak = new AltanakTheThriceCalled();
        harness.setGraveyard(player1, List.of(source, other, third, altanak));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Altanak, the Thrice-Called");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).filteredOn(exiled -> exiled.card().getName().equals("Say Its Name"))
                .hasSize(3);
    }

    @Test
    @DisplayName("The graveyard ability requires two other named cards")
    void requiresTwoOtherNamedCards() {
        SayItsName source = new SayItsName();
        SayItsName other = new SayItsName();
        harness.setGraveyard(player1, List.of(source, other, new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3).contains(source, other);
    }
}
