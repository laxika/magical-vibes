package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WanderingChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage enables the discard-and-draw ability with a blue permanent")
    void bluePermanentEnablesDiscardAndDraw() {
        addColoredPermanent(CardColor.BLUE);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        attackWithChampionDealingDamage();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Combat damage enables the discard-and-draw ability with a red permanent")
    void redPermanentEnablesDiscardAndDraw() {
        addColoredPermanent(CardColor.RED);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        attackWithChampionDealingDamage();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Combat damage does not trigger without a blue or red permanent")
    void noColoredPermanentDoesNotTrigger() {
        harness.setHand(player1, List.of(new GrizzlyBears()));

        attackWithChampionDealingDamage();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void addColoredPermanent(CardColor color) {
        Card card = new Card();
        card.setName(color + " Permanent");
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(1);
        card.setToughness(1);
        harness.addToBattlefield(player1, card);
    }

    private void attackWithChampionDealingDamage() {
        Permanent champion = addCreatureReady(player1, new WanderingChampion());
        champion.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }
}
