package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ManabondTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting reveals the hand, puts all lands onto the battlefield, and discards the rest")
    void acceptingPutsAllLandsOntoBattlefieldAndDiscardsRest() {
        harness.addToBattlefield(player1, new Manabond());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new LightningBolt()));

        resolveManabondTrigger();
        harness.handleMayAbilityChosen(player1, true);

        List<Card> battlefieldCards = gd.playerBattlefields.get(player1.getId()).stream()
                .map(permanent -> permanent.getCard())
                .toList();
        assertThat(battlefieldCards).filteredOn(card -> card.hasType(CardType.LAND)).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(CardType.LAND)))
                .allMatch(permanent -> !permanent.isTapped());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof LightningBolt);
    }

    @Test
    @DisplayName("Declining leaves the hand and battlefield unchanged")
    void decliningDoesNothing() {
        harness.addToBattlefield(player1, new Manabond());
        harness.setHand(player1, List.of(new Forest(), new LightningBolt()));

        resolveManabondTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof Forest);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card instanceof LightningBolt);
    }

    @Test
    @DisplayName("Accepting with no land cards still discards the hand")
    void acceptingWithoutLandsDiscardsHand() {
        harness.addToBattlefield(player1, new Manabond());
        harness.setHand(player1, List.of(new LightningBolt()));

        resolveManabondTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof LightningBolt);
    }

    private void resolveManabondTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
