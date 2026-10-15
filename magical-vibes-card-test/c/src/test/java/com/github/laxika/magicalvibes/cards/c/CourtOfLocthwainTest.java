package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CourtOfLocthwain.class, Forest.class, GrizzlyBears.class})
class CourtOfLocthwainTest extends BaseCardTest {

    @Test
    @DisplayName("Enters as the monarch and exiles the target opponent's top card during upkeep")
    void becomesMonarchAndExilesTargetOpponentsTopCard() {
        Permanent court = castCourt();
        Card ownTop = new Forest();
        Card opponentTop = new GrizzlyBears();
        harness.setLibrary(player1, List.of(ownTop));
        harness.setLibrary(player2, List.of(opponentTop));

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class))
                .isNotNull();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.PermanentChosen(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(court.getId())).containsExactly(opponentTop);
        assertThat(gd.findExiledCard(opponentTop.getId())).extracting(ExiledCardEntry::faceDown)
                .isEqualTo(false);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownTop);
    }

    @Test
    @DisplayName("The controller may cast an exiled spell with any color of mana")
    void castsExiledSpellWithAnyMana() {
        Permanent court = addCourtWithoutMakingPlayer1Monarch();
        Card exiled = new GrizzlyBears();
        harness.setLibrary(player2, List.of(exiled));

        resolveUpkeep(court);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castFromExile(player1, exiled.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The monarch may cast one exiled spell without paying its mana cost until end of turn")
    void monarchMayCastExiledSpellForFree() {
        Permanent court = castCourt();
        Card exiled = new GrizzlyBears();
        harness.setLibrary(player2, List.of(exiled));

        resolveUpkeep(court);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, exiled.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The upkeep ability cannot target its controller")
    void cannotTargetController() {
        Permanent court = harness.addToBattlefieldAndReturn(player1, new CourtOfLocthwain());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        gd.monarchPlayerId = player2.getId();

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class))
                .isNotNull();
        assertThatThrownBy(() -> harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.PermanentChosen(player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent castCourt() {
        harness.setHand(player1, List.of(new CourtOfLocthwain()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        return findPermanent(player1, "Court of Locthwain");
    }

    private void resolveUpkeep(Permanent court) {
        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class))
                .isNotNull();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.PermanentChosen(player2.getId()));
        harness.passBothPriorities();
    }

    private Permanent addCourtWithoutMakingPlayer1Monarch() {
        Permanent court = harness.addToBattlefieldAndReturn(player1, new CourtOfLocthwain());
        gd.monarchPlayerId = player2.getId();
        return court;
    }
}
