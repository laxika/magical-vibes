package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BartizanBats;
import com.github.laxika.magicalvibes.cards.d.DirectCurrent;
import com.github.laxika.magicalvibes.cards.i.InescapableBlaze;
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

@CardUsed({MissionBriefing.class, BartizanBats.class, InescapableBlaze.class, DirectCurrent.class})
class MissionBriefingTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils before offering only an instant or sorcery from your graveyard")
    void surveilsThenOffersFilteredGraveyardChoice() {
        Card topCard = new BartizanBats();
        Card secondCard = new BartizanBats();
        InescapableBlaze blaze = new InescapableBlaze();
        InescapableBlaze secondBlaze = new InescapableBlaze();
        BartizanBats creature = new BartizanBats();
        harness.setGraveyard(player1, List.of(blaze, creature, secondBlaze));
        harness.setGraveyard(player2, List.of(new InescapableBlaze()));

        resolveMissionBriefing(topCard, secondCard);

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0, 1);
        harness.handleGraveyardCardChosen(player1, 1);
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castFromGraveyardTargeting(player1, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(secondBlaze);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(blaze);
    }

    @Test
    @DisplayName("Casts the chosen graveyard spell later that turn and exiles it")
    void castsChosenSpellLaterAndExilesIt() {
        InescapableBlaze blaze = new InescapableBlaze();
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(blaze));

        resolveMissionBriefing(new BartizanBats(), new BartizanBats());

        harness.addMana(player1, ManaColor.RED, 6);
        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
        harness.assertNotInGraveyard(player1, "Inescapable Blaze");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(blaze.getId()));
    }

    @Test
    @DisplayName("The chosen card remains in the graveyard until cast")
    void chosenCardRemainsInGraveyardUntilCast() {
        InescapableBlaze blaze = new InescapableBlaze();
        harness.setGraveyard(player1, List.of(blaze));

        resolveMissionBriefing(new BartizanBats(), new BartizanBats());

        harness.assertInGraveyard(player1, "Inescapable Blaze");
        assertThat(gd.getPlayerExiledCards(player1.getId())).noneMatch(card -> card.getId().equals(blaze.getId()));
    }

    @Test
    @DisplayName("A spell just surveilled into the graveyard can be chosen and cast")
    void castsNewlySurveilledSpell() {
        InescapableBlaze blaze = new InescapableBlaze();
        harness.setGraveyard(player1, List.of());
        harness.setLife(player2, 20);

        resolveMissionBriefing(new BartizanBats(), blaze);

        harness.addMana(player1, ManaColor.RED, 6);
        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(blaze);
    }

    @Test
    @DisplayName("No eligible card after surveilling finishes resolving without choosing itself")
    void noEligibleCardDoesNotChooseMissionBriefingItself() {
        harness.setGraveyard(player1, List.of());

        resolveMissionBriefing(new BartizanBats(), new BartizanBats());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Mission Briefing");
        harness.addMana(player1, ManaColor.BLUE, 2);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Choosing a card does not waive its mana cost")
    void chosenSpellStillRequiresMana() {
        harness.setGraveyard(player1, List.of(new InescapableBlaze()));
        resolveMissionBriefing(new BartizanBats(), new BartizanBats());

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Inescapable Blaze");
    }

    @Test
    @DisplayName("An empty library does not prevent choosing and casting a graveyard spell")
    void emptyLibraryStillGrantsPermission() {
        InescapableBlaze spell = new InescapableBlaze();
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(new MissionBriefing()));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("A chosen jump-start sorcery may be cast for its normal cost without discarding")
    void chosenJumpStartSpellDoesNotRequireDiscard() {
        DirectCurrent spell = new DirectCurrent();
        harness.setGraveyard(player1, List.of(spell));
        harness.setLife(player2, 20);
        resolveMissionBriefing(new BartizanBats(), new BartizanBats());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("The casting permission expires at the end of the turn")
    void castingPermissionExpires() {
        harness.setGraveyard(player1, List.of(new InescapableBlaze()));
        resolveMissionBriefing(new BartizanBats(), new BartizanBats());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 6);

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Inescapable Blaze");
    }

    private void resolveMissionBriefing(Card topCard, Card secondCard) {
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player1, List.of(new MissionBriefing()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of(1)));
    }
}
