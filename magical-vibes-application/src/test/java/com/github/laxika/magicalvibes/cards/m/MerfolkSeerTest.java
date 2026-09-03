package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.n.NettletoothDjinn;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MerfolkSeer.class, NettletoothDjinn.class})
class MerfolkSeerTest extends BaseCardTest {

    /**
     * Puts Merfolk Seer on the battlefield blocking a lethal 4/4 attacker, advances to combat
     * damage so it dies, then resolves the queued death trigger up to the may-pay prompt.
     */
    private Card killInCombatUntilMayPrompt() {
        Permanent seer = addCreatureReady(player1, new MerfolkSeer());
        seer.setBlocking(true);
        seer.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new NettletoothDjinn());
        attacker.setAttacking(true);

        resolveCombat(player2);
        harness.passBothPriorities(); // resolve death trigger → may-pay prompt
        return seer.getCard();
    }

    @Test
    @DisplayName("Dies, pay {1}{U}, draws a card")
    void diesPayDrawsCard() {
        Card drawnCard = new NettletoothDjinn();
        harness.setLibrary(player1, List.of(drawnCard));

        Card seerCard = killInCombatUntilMayPrompt();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(seerCard.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(drawnCard.getId()));
    }

    @Test
    @DisplayName("Dies, decline paying {1}{U}, no card is drawn")
    void diesDeclineDrawsNothing() {
        Card drawnCard = new NettletoothDjinn();
        harness.setLibrary(player1, List.of(drawnCard));

        killInCombatUntilMayPrompt();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(drawnCard.getId()));
    }

    @Test
    @DisplayName("Dies, accept paying {1}{U} without enough mana, no card is drawn")
    void diesAcceptWithoutEnoughManaDrawsNothing() {
        Card drawnCard = new NettletoothDjinn();
        harness.setLibrary(player1, List.of(drawnCard));

        killInCombatUntilMayPrompt();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(drawnCard.getId()));
    }
}
