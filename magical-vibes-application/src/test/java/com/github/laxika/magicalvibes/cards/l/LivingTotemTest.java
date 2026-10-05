package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LivingTotem.class, RuneclawBear.class, LightningStrike.class})
class LivingTotemTest extends BaseCardTest {

    private void castTotemAndResolveSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new LivingTotem(), "{3}{G}");
        harness.passBothPriorities(); // resolve creature spell
    }

    @Test
    @DisplayName("Accepting the ETB may puts a +1/+1 counter on the chosen creature")
    void etbPutsCounterOnAnotherCreature() {
        harness.addToBattlefield(player1, new RuneclawBear());
        UUID bearsId = harness.getPermanentId(player1, "Runeclaw Bear");

        castTotemAndResolveSpell();
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent bears = findPermanent(player1, "Runeclaw Bear");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("The counter can be put on a creature an opponent controls")
    void etbCanTargetOpponentCreature() {
        harness.addToBattlefield(player2, new RuneclawBear());
        UUID bearsId = harness.getPermanentId(player2, "Runeclaw Bear");

        castTotemAndResolveSpell();
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player2, "Runeclaw Bear").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the ETB may leaves every creature without a counter")
    void decliningMayDoesNotPutCounter() {
        harness.addToBattlefield(player1, new RuneclawBear());
        UUID bearsId = harness.getPermanentId(player1, "Runeclaw Bear");

        castTotemAndResolveSpell();
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Runeclaw Bear").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Living Totem itself is not a legal choice for its own trigger")
    void cannotTargetItself() {
        harness.addToBattlefield(player1, new RuneclawBear());
        UUID bearsId = harness.getPermanentId(player1, "Runeclaw Bear");

        castTotemAndResolveSpell();

        UUID totemId = harness.getPermanentId(player1, "Living Totem");
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(bearsId).doesNotContain(totemId);
    }

    @Test
    @DisplayName("Can be cast with no other creature on the battlefield")
    void canBeCastWithNoOtherCreature() {
        castTotemAndResolveSpell();

        harness.assertOnBattlefield(player1, "Living Totem");
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Living Totem"));
    }

    @Test
    @DisplayName("Summoning-sick creatures can convoke the entire cost and receive the counter")
    void convokePaysEntireCost() {
        List<Permanent> creatures = java.util.stream.IntStream.range(0, 4)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new RuneclawBear()))
                .toList();
        creatures.forEach(creature -> creature.setSummoningSick(true));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LivingTotem()));

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                creatures.stream().map(Permanent::getId).toList());

        assertThat(creatures).allMatch(Permanent::isTapped);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Living Totem");
        harness.handlePermanentChosen(player1, creatures.getFirst().getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creatures.getFirst().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(creatures.subList(1, 4)).allMatch(creature ->
                creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 0);
    }

    @Test
    @DisplayName("Another Living Totem is a legal target")
    void canTargetAnotherLivingTotem() {
        Permanent otherTotem = harness.addToBattlefieldAndReturn(player1, new LivingTotem());

        castTotemAndResolveSpell();
        harness.handlePermanentChosen(player1, otherTotem.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(otherTotem.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Living Totem")).hasSize(2);
        assertThat(findPermanents(player1, "Living Totem").getLast()
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The trigger does not resolve when its target leaves the battlefield")
    void targetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        castTotemAndResolveSpell();
        harness.handlePermanentChosen(player1, target.getId());
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Runeclaw Bear");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanent(player1, "Living Totem")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The counter trigger still resolves after Living Totem dies")
    void sourceLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        castTotemAndResolveSpell();
        harness.handlePermanentChosen(player1, target.getId());
        UUID totemId = harness.getPermanentId(player1, "Living Totem");
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, totemId);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Living Totem");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
