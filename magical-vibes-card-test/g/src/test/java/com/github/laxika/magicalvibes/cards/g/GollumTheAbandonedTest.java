package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GollumTheAbandoned.class, GoblinTownFlunkies.class, GoblinPlateMail.class})
class GollumTheAbandonedTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles up to one card from an opponent's graveyard and each opponent loses 2 life")
    void etbExilesCardAndEachOpponentLosesLife() {
        Card graveyardCard = new GoblinTownFlunkies();
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.setHand(player1, List.of(new GollumTheAbandoned()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(card -> card.getId().equals(graveyardCard.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId())).anyMatch(card -> card.getId().equals(graveyardCard.getId()));
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("ETB may exile no card and still makes each opponent lose 2 life")
    void etbMayExileNoCardAndStillMakesEachOpponentLoseLife() {
        Card graveyardCard = new GoblinTownFlunkies();
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.setHand(player1, List.of(new GollumTheAbandoned()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(graveyardCard);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The return ability cannot be activated while Gollum is on the battlefield")
    void returnAbilityCannotBeActivatedFromBattlefield() {
        Permanent gollum = harness.addToBattlefieldAndReturn(player1, new GollumTheAbandoned());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(gollum);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(gollum.getCard());
    }

    @Test
    @DisplayName("Gollum cannot block")
    void cannotBlock() {
        Permanent gollum = harness.addToBattlefieldAndReturn(player1, new GollumTheAbandoned());

        assertThat(bls.canBlock(gd, gollum)).isFalse();
    }

    @Test
    @DisplayName("Sacrificing a creature returns Gollum from the graveyard to hand")
    void returnsFromGraveyardBySacrificingCreature() {
        Card gollum = new GollumTheAbandoned();
        harness.setGraveyard(player1, List.of(gollum));
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GoblinTownFlunkies());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrifice);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(gollum, sacrifice.getCard());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(gollum);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(gollum);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard()).doesNotContain(gollum);
    }

    @Test
    @DisplayName("Sacrificing a noncreature artifact returns Gollum from the graveyard to hand")
    void returnsFromGraveyardBySacrificingArtifact() {
        Card gollum = new GollumTheAbandoned();
        harness.setGraveyard(player1, List.of(gollum));
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GoblinPlateMail());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrifice);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard()).doesNotContain(gollum);
        assertThat(gd.playerHands.get(player1.getId())).contains(gollum);
    }

    @Test
    @DisplayName("The graveyard ability requires a permanent controlled by its activator to sacrifice")
    void cannotSacrificeOpponentsCreature() {
        Card gollum = new GollumTheAbandoned();
        harness.setGraveyard(player1, List.of(gollum));
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GoblinTownFlunkies());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(gollum);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(gollum);
    }

    @Test
    @DisplayName("The graveyard ability cannot be activated during combat")
    void graveyardAbilityRequiresSorceryTiming() {
        Card gollum = new GollumTheAbandoned();
        harness.setGraveyard(player1, List.of(gollum));
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GoblinTownFlunkies());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrifice);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(gollum);
    }

    @Test
    @DisplayName("The entire ETB ability fails to resolve when its only target leaves the graveyard")
    void illegalOnlyTargetPreventsLifeLoss() {
        Card graveyardCard = new GoblinTownFlunkies();
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.setHand(player1, List.of(new GollumTheAbandoned()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(graveyardCard));
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("ETB cannot target the controller's graveyard or more than one card")
    void etbRejectsOwnGraveyardAndMultipleTargets() {
        Card ownCard = new GoblinTownFlunkies();
        Card opponentCard = new GoblinTownFlunkies();
        Card secondOpponentCard = new GoblinPlateMail();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard, secondOpponentCard));
        harness.setHand(player1, List.of(new GollumTheAbandoned()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(ownCard.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(opponentCard.getId(), secondOpponentCard.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(opponentCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(secondOpponentCard).doesNotContain(opponentCard);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("ETB still causes life loss when the opponent's graveyard is empty")
    void emptyOpponentGraveyardStillCausesLifeLoss() {
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new GollumTheAbandoned()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }
}
