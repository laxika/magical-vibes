package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.VivienOnTheHunt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiveteersCharm.class, GrizzlyBears.class, HillGiant.class, Forest.class,
        Shock.class, VivienOnTheHunt.class})
class RiveteersCharmTest extends BaseCardTest {

    @Test
    @DisplayName("The sacrifice mode sacrifices only a creature or planeswalker with greatest mana value")
    void sacrificesGreatestManaValuePermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());

        cast(0, player2.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("The top-card mode exiles three cards and allows one to be played from exile")
    void exilesTopThreeAndAllowsPlayingThem() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        GrizzlyBears third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third));

        cast(1, null);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactly(first, second, third);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, second.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, third);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The graveyard mode exiles any target player's graveyard")
    void exilesTargetPlayersGraveyard() {
        GrizzlyBears card = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(card));

        cast(2, player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(card);
    }

    @Test
    @DisplayName("The sacrifice mode cannot target its controller")
    void sacrificeModeRequiresAnOpponent() {
        harness.setHand(player1, List.of(new RiveteersCharm()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentChoosesAmongTiedGreatestManaValues() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        var first = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        var second = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        cast(0, player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first).doesNotContain(second);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void sacrificesPlaneswalkerWhenItsManaValueIsGreatest() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.enterBattlefieldAndReturn(player2, new VivienOnTheHunt());

        cast(0, player2.getId());

        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Vivien on the Hunt");
        harness.assertInGraveyard(player2, "Vivien on the Hunt");
    }

    @Test
    void sacrificeModeCanTargetOpponentWithoutEligiblePermanents() {
        harness.addToBattlefield(player2, new Forest());

        cast(0, player2.getId());

        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Riveteers Charm");
    }

    @Test
    void exilesOnlyAvailableCardsFromAShortLibrary() {
        var first = new GrizzlyBears();
        var second = new HillGiant();
        harness.setLibrary(player1, List.of(first, second));

        cast(1, null);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
    }

    @Test
    void canPlayAnExiledLandButCannotPlayASecondLandThatTurn() {
        var first = new Forest();
        var second = new Forest();
        harness.setLibrary(player1, List.of(first, second));

        cast(1, null);
        harness.castFromExile(player1, first.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(second);
    }

    @Test
    void cannotCastExiledInstantOnceNextEndStepBegins() {
        var card = new Shock();
        harness.setLibrary(player1, List.of(card));
        cast(1, null);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, card.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(card);
    }

    @Test
    void castingDuringOwnEndStepAllowsPlayingCardsOnFollowingTurn() {
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        var card = new GrizzlyBears();
        harness.setLibrary(player1, List.of(card));
        cast(1, null);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void graveyardModeCanTargetControllerAndLeavesOpponentsGraveyardAlone() {
        var first = new GrizzlyBears();
        var second = new HillGiant();
        var opponentCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setGraveyard(player2, List.of(opponentCard));

        cast(2, player1.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(1).allMatch(card -> card instanceof RiveteersCharm);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
    }

    private void cast(int mode, UUID targetId) {
        harness.setHand(player1, List.of(new RiveteersCharm()));
        addMana();
        harness.castInstant(player1, 0, mode, targetId);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
