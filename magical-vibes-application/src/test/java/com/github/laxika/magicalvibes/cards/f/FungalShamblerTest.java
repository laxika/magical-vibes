package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FungalShambler.class, Forest.class, GrizzlyBears.class})
class FungalShamblerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage draws a card, then the damaged opponent discards a card")
    void combatDamageDrawsAndDamagedOpponentDiscards() {
        Permanent shambler = addCreatureReady(player1, new FungalShambler());
        shambler.setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));
        int controllerHandBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombatAndTrigger();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandBefore + 1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Noncombat damage also causes the draw and discard trigger")
    void noncombatDamageTriggers() {
        FungalShambler card = new FungalShambler();
        card.addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new DealDamageToAnyTargetEffect(1)),
                "{T}: This creature deals 1 damage to any target."));
        Permanent shambler = addCreatureReady(player1, card);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));
        int controllerHandBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(shambler),
                null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandBefore + 1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
