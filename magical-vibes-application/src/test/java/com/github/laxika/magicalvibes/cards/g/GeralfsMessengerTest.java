package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

class GeralfsMessengerTest extends BaseCardTest {

    /** Resolves the stack until the game pauses for input or the stack empties. */
    private void resolveUntilInputOrEmpty() {
        for (int i = 0; i < 12; i++) {
            GameData gd = harness.getGameData();
            if (gd.interaction.isAwaitingInput() || gd.stack.isEmpty()) {
                return;
            }
            harness.passBothPriorities();
        }
    }

    private Permanent messengerOnBattlefield() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Geralf's Messenger"))
                .findFirst().orElse(null);
    }

    private void castMessengerTargetingOpponent() {
        harness.setHand(player1, List.of(new GeralfsMessenger()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.getGameService().playCard(gd, player1, 0, 0, player2.getId(), null);
    }

    // ===== Enters tapped =====

    @Test
    @DisplayName("Geralf's Messenger enters the battlefield tapped")
    void entersBattlefieldTapped() {
        castMessengerTargetingOpponent();
        harness.passBothPriorities(); // resolve creature spell

        Permanent messenger = messengerOnBattlefield();
        assertThat(messenger).isNotNull();
        assertThat(messenger.isTapped()).isTrue();
    }

    // ===== ETB life loss =====

    @Test
    @DisplayName("ETB trigger makes target opponent lose 2 life")
    void etbMakesTargetOpponentLoseLife() {
        castMessengerTargetingOpponent();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot cast by targeting yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new GeralfsMessenger()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, player1.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    // ===== Undying =====

    @Test
    @DisplayName("Undying returns Geralf's Messenger with a +1/+1 counter when it dies with no counters")
    void undyingReturnsWithCounter() {
        harness.addToBattlefield(player1, new GeralfsMessenger());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Geralf's Messenger"));
        resolveUntilInputOrEmpty();

        // Bolt killed the 3/2 Messenger; undying returned it with a +1/+1 counter and its
        // ETB ability is now asking for an opponent target.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        Permanent messenger = messengerOnBattlefield();
        assertThat(messenger).isNotNull();
        assertThat(messenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(messenger.getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("Undying return re-triggers the ETB, making the chosen opponent lose 2 life")
    void undyingReturnRetriggersEtb() {
        harness.addToBattlefield(player1, new GeralfsMessenger());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Geralf's Messenger"));
        resolveUntilInputOrEmpty();

        // Choose the opponent as the target of the returned Messenger's ETB.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveUntilInputOrEmpty();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Undying does not return Geralf's Messenger when it died with a +1/+1 counter")
    void undyingDoesNotReturnWithCounter() {
        Permanent messenger = harness.addToBattlefieldAndReturn(player1, new GeralfsMessenger());
        messenger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1); // now 4/3 — Lightning Bolt's 3 damage is lethal
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, messenger.getId());
        resolveUntilInputOrEmpty();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Geralf's Messenger"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Geralf's Messenger"));
    }
}
