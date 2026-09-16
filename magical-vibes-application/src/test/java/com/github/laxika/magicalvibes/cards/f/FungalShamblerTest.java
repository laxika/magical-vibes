package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.k.KavuGlider;
import com.github.laxika.magicalvibes.cards.q.QuicksilverDagger;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FungalShambler.class, KavuGlider.class, QuicksilverDagger.class})
class FungalShamblerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage draws a card, then the damaged opponent discards a card")
    void combatDamageDrawsAndDamagedOpponentDiscards() {
        Permanent shambler = addCreatureReady(player1, new FungalShambler());
        shambler.setAttacking(true);
        harness.setLibrary(player1, List.of(new KavuGlider()));
        harness.setHand(player2, List.of(new KavuGlider(), new KavuGlider()));
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
        Permanent shambler = addDaggeredShambler();
        harness.setLibrary(player1, List.of(new KavuGlider(), new KavuGlider()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new KavuGlider(), new KavuGlider()));

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(shambler),
                null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger when noncombat damage is dealt to its controller")
    void noncombatDamageToControllerDoesNotTrigger() {
        Permanent shambler = addDaggeredShambler();
        harness.setLibrary(player1, List.of(new KavuGlider(), new KavuGlider()));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(shambler),
                null, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addDaggeredShambler() {
        Permanent shambler = addCreatureReady(player1, new FungalShambler());
        Permanent dagger = harness.addToBattlefieldAndReturn(player1, new QuicksilverDagger());
        dagger.setAttachedTo(shambler.getId());
        return shambler;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
