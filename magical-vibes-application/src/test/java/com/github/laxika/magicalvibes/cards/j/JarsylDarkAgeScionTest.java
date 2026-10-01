package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JarsylDarkAgeScion.class, DarkRitual.class, GrizzlyBears.class})
class JarsylDarkAgeScionTest extends BaseCardTest {

    @Test
    void castsSpellAtCurrentIntensityAndIntensifiesAfterSuccessfulCast() {
        JarsylDarkAgeScion jarsylCard = new JarsylDarkAgeScion();
        Permanent jarsyl = harness.addToBattlefieldAndReturn(player1, jarsylCard);
        DarkRitual darkRitual = new DarkRitual();
        GrizzlyBears grizzlyBears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(darkRitual, grizzlyBears));

        triggerBeginningOfCombat(player1);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.getSpellsCastThisTurn(player1.getId()))
                .anyMatch(card -> card.getId().equals(darkRitual.getId()));
        assertThat(gd.getCardIntensity(jarsyl.getCard().getId())).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(grizzlyBears);
    }

    @Test
    void onlyOffersCardsWithManaValueEqualToIntensity() {
        JarsylDarkAgeScion jarsylCard = new JarsylDarkAgeScion();
        Permanent jarsyl = harness.addToBattlefieldAndReturn(player1, jarsylCard);
        GrizzlyBears grizzlyBears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(grizzlyBears));
        gd.intensifyCard(jarsylCard, 1);

        triggerBeginningOfCombat(player1);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getCardIntensity(jarsyl.getCard().getId())).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(grizzlyBears);
    }

    @Test
    void decliningTheCastDoesNotIntensifyJarsyl() {
        JarsylDarkAgeScion jarsylCard = new JarsylDarkAgeScion();
        Permanent jarsyl = harness.addToBattlefieldAndReturn(player1, jarsylCard);
        DarkRitual darkRitual = new DarkRitual();
        harness.setGraveyard(player1, List.of(darkRitual));

        triggerBeginningOfCombat(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getCardIntensity(jarsyl.getCard().getId())).isEqualTo(1);
        assertThat(gd.getSpellsCastThisTurn(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(darkRitual);
    }

    private void triggerBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(StepTriggerService.class)
                .handleBeginningOfCombatTriggers(gd));
    }
}
