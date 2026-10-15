package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheReaperKingNoMore.class, GrizzlyBears.class, Plains.class, Shock.class})
class TheReaperKingNoMoreTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with -1/-1 counters on up to two target creatures")
    void putsCountersOnTwoTargetCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castWithTargets(List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can enter without choosing targets")
    void canEnterWithoutTargets() {
        castWithTargets(List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "The Reaper, King No More");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Rejects a noncreature ETB target")
    void rejectsNoncreatureTarget() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());

        assertThatThrownBy(() -> castWithTargets(List.of(plains.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("May return an opponent creature with a -1/-1 counter")
    void returnsCounteredOpponentCreature() {
        harness.addToBattlefield(player1, new TheReaperKingNoMore());
        Permanent dying = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        dying.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        destroyWithShock(dying);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot return another creature after returning one this turn")
    void returnsOnlyOneCreatureEachTurn() {
        harness.addToBattlefield(player1, new TheReaperKingNoMore());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        first.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        second.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        destroyWithShock(first);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        destroyWithShock(second);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining a return does not consume the turn's allowance")
    void canReturnLaterCreatureAfterDeclining() {
        harness.addToBattlefield(player1, new TheReaperKingNoMore());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        first.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        second.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        destroyWithShock(first);
        harness.handleMayAbilityChosen(player1, false);
        destroyWithShock(second);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(second.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(first.getCard()).doesNotContain(second.getCard());
    }

    @Test
    @DisplayName("Both deaths trigger when no creature has been returned yet")
    void triggersForAnotherDeathBeforeFirstReturnResolves() {
        harness.addToBattlefield(player1, new TheReaperKingNoMore());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        first.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        second.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, first.getId());
        harness.castAndResolveInstant(player1, 0, second.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Returns a creature you own that died under an opponent's control")
    void returnsOwnedCreatureControlledByOpponent() {
        harness.addToBattlefield(player1, new TheReaperKingNoMore());
        GrizzlyBears bears = new GrizzlyBears();
        bears.setOwnerId(player1.getId());
        Permanent dying = harness.addToBattlefieldAndReturn(player2, bears);
        dying.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        destroyWithShock(dying);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An opponent creature without a -1/-1 counter does not trigger a return")
    void doesNotReturnUncounteredCreature() {
        harness.addToBattlefield(player1, new TheReaperKingNoMore());
        Permanent dying = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        destroyWithShock(dying);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Your own controlled creature with a -1/-1 counter does not trigger a return")
    void doesNotReturnOwnControlledCreature() {
        harness.addToBattlefield(player1, new TheReaperKingNoMore());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        dying.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        destroyWithShock(dying);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can put a counter on just one target creature")
    void putsCounterOnOneTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castWithTargets(List.of(target.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    private void castWithTargets(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new TheReaperKingNoMore()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0, targetIds);
    }

    private void destroyWithShock(Permanent target) {
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
