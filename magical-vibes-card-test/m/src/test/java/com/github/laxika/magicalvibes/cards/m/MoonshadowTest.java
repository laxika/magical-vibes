package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DampenThought;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Moonshadow.class, DampenThought.class, Shock.class, Spellbook.class})
class MoonshadowTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with six -1/-1 counters")
    void entersWithSixMinusCounters() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Moonshadow()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent moonshadow = findPermanent(player1, "Moonshadow");
        assertThat(moonshadow.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("A permanent card put into its controller's graveyard removes a counter")
    void permanentCardToOwnGraveyardRemovesCounter() {
        Permanent moonshadow = addMoonshadowWithCounters(2);
        Permanent spellbook = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, spellbook));
        resolveAllTriggers();

        assertThat(moonshadow.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A nonpermanent card does not trigger Moonshadow")
    void nonpermanentCardDoesNotTrigger() {
        Permanent moonshadow = addMoonshadowWithCounters(2);
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock()));
        harness.setHand(player1, List.of(new DampenThought()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(moonshadow.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's permanent card does not trigger Moonshadow")
    void opponentGraveyardDoesNotTrigger() {
        Permanent moonshadow = addMoonshadowWithCounters(2);
        Permanent spellbook = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, spellbook));

        assertThat(gd.stack).isEmpty();
        assertThat(moonshadow.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The trigger does nothing if the last counter is removed before resolution")
    void triggerDoesNothingAfterCounterIsRemoved() {
        Permanent moonshadow = addMoonshadowWithCounters(1);
        Permanent spellbook = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, spellbook));
        moonshadow.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
        resolveAllTriggers();

        assertThat(moonshadow.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A graveyard card does not trigger Moonshadow without a counter")
    void doesNotTriggerWithoutCounters() {
        Permanent moonshadow = addMoonshadowWithCounters(0);
        Permanent spellbook = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, spellbook));

        assertThat(gd.stack).isEmpty();
        assertThat(moonshadow.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Milling several permanent cards together removes only one counter")
    void simultaneousPermanentCardsRemoveOnlyOneCounter() {
        Permanent moonshadow = addMoonshadowWithCounters(6);
        harness.setLibrary(player1, List.of(new Moonshadow(), new Moonshadow(),
                new Moonshadow(), new Moonshadow()));
        harness.setHand(player1, List.of(new DampenThought()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(moonshadow.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("A single permanent card milled from the library removes a counter")
    void singlePermanentCardMilledRemovesCounter() {
        Permanent moonshadow = addMoonshadowWithCounters(2);
        harness.setLibrary(player1, List.of(new Moonshadow(), new Shock(), new Shock(), new Shock()));
        harness.setHand(player1, List.of(new DampenThought()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(moonshadow.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Separate graveyard events each remove a counter")
    void separateGraveyardEventsEachRemoveCounter() {
        Permanent moonshadow = addMoonshadowWithCounters(3);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, first));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, second));

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        assertThat(moonshadow.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A token going to the graveyard does not trigger Moonshadow")
    void tokenDoesNotTrigger() {
        Permanent moonshadow = addMoonshadowWithCounters(2);
        Moonshadow tokenCard = new Moonshadow();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, token));

        assertThat(gd.stack).isEmpty();
        assertThat(moonshadow.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    private Permanent addMoonshadowWithCounters(int count) {
        Permanent moonshadow = harness.addToBattlefieldAndReturn(player1, new Moonshadow());
        moonshadow.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, count);
        return moonshadow;
    }
}
