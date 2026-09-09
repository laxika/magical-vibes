package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SkyshroudForest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Manabond.class, Forest.class, SkyshroudForest.class, DarkRitual.class})
class ManabondTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting reveals the hand, puts all lands onto the battlefield, and discards the rest")
    void acceptingPutsAllLandsOntoBattlefieldAndDiscardsRest() {
        harness.addToBattlefield(player1, new Manabond());
        Forest forest = new Forest();
        SkyshroudForest skyshroudForest = new SkyshroudForest();
        DarkRitual darkRitual = new DarkRitual();
        harness.setHand(player1, List.of(forest, skyshroudForest, darkRitual));

        resolveManabondTrigger();
        harness.handleMayAbilityChosen(player1, true);

        List<Card> battlefieldCards = gd.playerBattlefields.get(player1.getId()).stream()
                .map(permanent -> permanent.getCard())
                .toList();
        assertThat(battlefieldCards).filteredOn(card -> card.hasType(CardType.LAND)).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == forest && !permanent.isTapped());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == skyshroudForest && permanent.isTapped());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(darkRitual);
        assertThat(gameLogContains("reveals their hand")).isTrue();
    }

    @Test
    @DisplayName("Declining leaves the hand and battlefield unchanged")
    void decliningDoesNothing() {
        harness.addToBattlefield(player1, new Manabond());
        Forest forest = new Forest();
        DarkRitual darkRitual = new DarkRitual();
        harness.setHand(player1, List.of(forest, darkRitual));

        resolveManabondTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof Forest);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest, darkRitual);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card == darkRitual);
    }

    @Test
    @DisplayName("Accepting with no land cards still discards the hand")
    void acceptingWithoutLandsDiscardsHand() {
        harness.addToBattlefield(player1, new Manabond());
        DarkRitual darkRitual = new DarkRitual();
        harness.setHand(player1, List.of(darkRitual));

        resolveManabondTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(darkRitual);
    }

    @Test
    @DisplayName("Triggers only during the controller's end step")
    void triggersOnlyDuringControllersEndStep() {
        harness.addToBattlefield(player1, new Manabond());
        Forest forest = new Forest();
        DarkRitual darkRitual = new DarkRitual();
        harness.setHand(player1, List.of(forest, darkRitual));

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest, darkRitual);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(darkRitual);
    }

    private void resolveManabondTrigger() {
        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
