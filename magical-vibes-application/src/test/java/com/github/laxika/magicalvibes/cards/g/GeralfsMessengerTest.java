package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({GeralfsMessenger.class, LightningBolt.class})
class GeralfsMessengerTest extends BaseCardTest {

    private Permanent messengerOnBattlefield() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Geralf's Messenger"))
                .findFirst().orElse(null);
    }

    private void castMessengerTargetingOpponent() {
        harness.setHand(player1, List.of(new GeralfsMessenger()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0, player2.getId());
    }

    @Test
    @DisplayName("Geralf's Messenger enters the battlefield tapped")
    void entersBattlefieldTapped() {
        castMessengerTargetingOpponent();
        harness.passBothPriorities(); // resolve creature spell

        Permanent messenger = messengerOnBattlefield();
        assertThat(messenger).isNotNull();
        assertThat(messenger.isTapped()).isTrue();
    }

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
    @DisplayName("The ETB cannot target its controller")
    void etbCannotTargetYourself() {
        harness.addToBattlefield(player1, new GeralfsMessenger());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Geralf's Messenger"));
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Undying returns Geralf's Messenger with a +1/+1 counter when it dies with no counters")
    void undyingReturnsWithCounter() {
        harness.addToBattlefield(player1, new GeralfsMessenger());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Geralf's Messenger"));
        resolveAllTriggers();

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
        resolveAllTriggers();

        // Choose the opponent as the target of the returned Messenger's ETB.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Undying does not return Geralf's Messenger when it died with a +1/+1 counter")
    void undyingDoesNotReturnWithCounter() {
        Permanent messenger = harness.addToBattlefieldAndReturn(player1, new GeralfsMessenger());
        messenger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1); // now 4/3 â€” Lightning Bolt's 3 damage is lethal
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, messenger.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Geralf's Messenger");
        harness.assertNotOnBattlefield(player1, "Geralf's Messenger");
    }

    @Test
    @DisplayName("Messenger can be cast without choosing its ETB target until it enters")
    void choosesTargetAfterEntering() {
        harness.setHand(player1, List.of(new GeralfsMessenger()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);
        harness.assertNotOnBattlefield(player1, "Geralf's Messenger");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(messengerOnBattlefield().isTapped()).isTrue();
        harness.assertLife(player2, 20);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Undying returns Messenger tapped with a new permanent identity")
    void undyingReturnsTappedAsNewPermanent() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new GeralfsMessenger());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, original.getId());
        resolveAllTriggers();

        Permanent returned = messengerOnBattlefield();
        assertThat(returned).isNotNull();
        assertThat(returned.getId()).isNotEqualTo(original.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Geralf's Messenger");
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The original ETB still resolves after Messenger dies and returns")
    void originalEtbSurvivesDeathAndReturn() {
        castMessengerTargetingOpponent();
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Geralf's Messenger"));
        resolveAllTriggers();

        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();
        harness.assertLife(player2, 16);
        harness.assertLife(player1, 20);
    }
}
