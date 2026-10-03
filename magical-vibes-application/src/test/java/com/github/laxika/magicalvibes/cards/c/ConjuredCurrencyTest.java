package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FlickerOfFate;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SelesnyaSentry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConjuredCurrency.class, SelesnyaSentry.class, Island.class, FlickerOfFate.class})
class ConjuredCurrencyTest extends BaseCardTest {

    @Test
    @DisplayName("Exchanges control of itself and the chosen permanent when accepted")
    void exchangesControlWhenAccepted() {
        harness.addToBattlefield(player1, new ConjuredCurrency());
        Permanent opp = harness.addToBattlefieldAndReturn(player2, new SelesnyaSentry());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, opp.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Selesnya Sentry");
        harness.assertNotOnBattlefield(player2, "Selesnya Sentry");
        harness.assertOnBattlefield(player2, "Conjured Currency");
        harness.assertNotOnBattlefield(player1, "Conjured Currency");
    }

    @Test
    @DisplayName("No exchange when the controller declines")
    void noExchangeWhenDeclined() {
        harness.addToBattlefield(player1, new ConjuredCurrency());
        Permanent opp = harness.addToBattlefieldAndReturn(player2, new SelesnyaSentry());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, opp.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Conjured Currency");
        harness.assertOnBattlefield(player2, "Selesnya Sentry");
    }

    @Test
    @DisplayName("Permanents its controller controls are not legal targets")
    void cannotTargetOwnPermanent() {
        harness.addToBattlefield(player1, new ConjuredCurrency());
        harness.addToBattlefield(player1, new SelesnyaSentry());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        // The only other permanent belongs to the controller, so there is nothing to exchange with.
        harness.assertOnBattlefield(player1, "Conjured Currency");
        harness.assertOnBattlefield(player1, "Selesnya Sentry");
    }

    @Test
    @DisplayName("The new controller cannot target a permanent they own but no longer control")
    void newControllerCannotTargetOwnedPermanent() {
        harness.addToBattlefield(player1, new ConjuredCurrency());
        Permanent sentry = harness.addToBattlefieldAndReturn(player2, new SelesnyaSentry());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, sentry.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // Player 2 now controls Conjured Currency, so on their upkeep it triggers for them. The
        // Selesnya Sentry player 1 took is still owned by player 2, so it is not a legal target
        // and nothing can be exchanged back.
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Selesnya Sentry");
        harness.assertOnBattlefield(player2, "Conjured Currency");
    }

    @Test
    @DisplayName("Can exchange control with a land")
    void exchangesControlWithLand() {
        harness.addToBattlefield(player1, new ConjuredCurrency());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, island.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Island");
        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertOnBattlefield(player2, "Conjured Currency");
        harness.assertNotOnBattlefield(player1, "Conjured Currency");
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerOnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new ConjuredCurrency());
        harness.addToBattlefield(player2, new SelesnyaSentry());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Conjured Currency");
        harness.assertOnBattlefield(player2, "Selesnya Sentry");
    }

    @Test
    @DisplayName("No exchange if the targeted permanent leaves and returns before resolution")
    void noExchangeWithReturnedTarget() {
        harness.addToBattlefield(player1, new ConjuredCurrency());
        Permanent sentry = harness.addToBattlefieldAndReturn(player2, new SelesnyaSentry());
        harness.setHand(player1, List.of(new FlickerOfFate()));

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, sentry.getId());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, sentry.getId());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Conjured Currency");
        harness.assertOnBattlefield(player2, "Selesnya Sentry");
        harness.assertNotOnBattlefield(player1, "Selesnya Sentry");
    }

    @Test
    @DisplayName("No exchange if Conjured Currency leaves and returns before resolution")
    void noExchangeWithReturnedSource() {
        Permanent currency = harness.addToBattlefieldAndReturn(player1, new ConjuredCurrency());
        Permanent sentry = harness.addToBattlefieldAndReturn(player2, new SelesnyaSentry());
        harness.setHand(player1, List.of(new FlickerOfFate()));

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, sentry.getId());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, currency.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Conjured Currency");
        harness.assertNotOnBattlefield(player2, "Conjured Currency");
        harness.assertOnBattlefield(player2, "Selesnya Sentry");
        harness.assertNotOnBattlefield(player1, "Selesnya Sentry");
    }
}
