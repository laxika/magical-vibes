package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AinokTracker;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnblinkingBleb.class, AinokTracker.class, Forest.class, GrizzlyBears.class})
class UnblinkingBlebTest extends BaseCardTest {

    @Test
    @DisplayName("Turning Unblinking Bleb face up may scry 2")
    void mayScryWhenItTurnsFaceUp() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        Permanent bleb = addFaceDownBleb(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bleb));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
    }

    @Test
    @DisplayName("Declining Unblinking Bleb's scry does nothing")
    void mayScryCanBeDeclined() {
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        Permanent bleb = addFaceDownBleb(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bleb));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest", "Grizzly Bears");
    }

    @Test
    @DisplayName("Unblinking Bleb triggers when an opponent's permanent turns face up")
    void triggersForAnOpponentsPermanent() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        harness.addToBattlefield(player1, new UnblinkingBleb());
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new AinokTracker());
        opponentPermanent.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.turnFaceUp(player2, gd.playerBattlefields.get(player2.getId()).indexOf(opponentPermanent));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
    }

    private Permanent addFaceDownBleb(com.github.laxika.magicalvibes.model.Player player) {
        Permanent bleb = harness.addToBattlefieldAndReturn(player, new UnblinkingBleb());
        bleb.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        return bleb;
    }
}
