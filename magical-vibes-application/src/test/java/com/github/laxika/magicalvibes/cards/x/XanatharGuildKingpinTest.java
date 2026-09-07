package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({XanatharGuildKingpin.class, Forest.class, GrizzlyBears.class, Shock.class})
class XanatharGuildKingpinTest extends BaseCardTest {

    private void resolveUpkeepTrigger(Card topCard) {
        harness.setLibrary(player2, List.of(topCard));
        harness.addToBattlefield(player1, new XanatharGuildKingpin());
        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.PermanentChosen(player2.getId()));
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The controller can cast the target opponent's top spell with any color of mana")
    void castsTargetOpponentsTopSpellWithAnyColorMana() {
        GrizzlyBears bears = new GrizzlyBears();
        resolveUpkeepTrigger(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("The controller can play the target opponent's top land")
    void playsTargetOpponentsTopLand() {
        Forest forest = new Forest();
        resolveUpkeepTrigger(forest);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(forest);
    }

    @Test
    @DisplayName("The chosen opponent can't cast spells until end of turn")
    void chosenOpponentCannotCastSpells() {
        resolveUpkeepTrigger(new Forest());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
