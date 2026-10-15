package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DarksteelGarrison;
import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.cards.v.VenserShaperSavant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PetrifiedPlating.class, NessianCourser.class, DarksteelGarrison.class,
        VenserShaperSavant.class, PithingNeedle.class})
class PetrifiedPlatingTest extends BaseCardTest {

    @Test
    @DisplayName("Petrified Plating gives only the enchanted creature +2/+2")
    void givesEnchantedCreatureBoost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NessianCourser());
        Permanent bystander = harness.addToBattlefieldAndReturn(player1, new NessianCourser());
        harness.setHand(player1, List.of(new PetrifiedPlating()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, bystander)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bystander)).isEqualTo(3);
    }

    @Test
    @DisplayName("Petrified Plating can enchant an opponent's creature")
    void canEnchantOpponentCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NessianCourser());
        harness.setHand(player1, List.of(new PetrifiedPlating()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Suspend exiles Petrified Plating with two time counters")
    void suspendExilesWithTwoTimeCounters() {
        PetrifiedPlating plating = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(plating);
        assertThat(gd.exiledCardTimeCounters).containsEntry(plating.getId(), 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A suspended Petrified Plating can be cast for free onto a creature")
    void suspendedCardCastsForFree() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NessianCourser());
        PetrifiedPlating plating = suspendCard();

        for (int i = 0; i < 2; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(plating);
    }

    @Test
    @DisplayName("An opponent's upkeep does not remove Petrified Plating's suspend counters")
    void opponentUpkeepDoesNotRemoveSuspendCounter() {
        PetrifiedPlating plating = suspendCard();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(plating.getId(), 2);
    }

    @Test
    @DisplayName("Declining the free suspend cast leaves Petrified Plating exiled")
    void decliningSuspendCastLeavesCardExiled() {
        PetrifiedPlating plating = suspendCard();

        for (int i = 0; i < 2; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(plating);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(plating.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == plating);
    }

    @Test
    @DisplayName("Petrified Plating cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent garrison = harness.addToBattlefieldAndReturn(player1, new DarksteelGarrison());
        harness.setHand(player1, List.of(new PetrifiedPlating()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, garrison.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void suspendCastWithoutCreatureTargetsLeavesCardExiled() {
        PetrifiedPlating plating = suspendCard();
        for (int i = 0; i < 2; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(plating);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(plating.getId());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({PetrifiedPlating.class, PithingNeedle.class})
    void namingPlatingWithPithingNeedleDoesNotPreventSuspend() {
        Permanent needle = harness.addToBattlefieldAndReturn(player2, new PithingNeedle());
        needle.setChosenName("Petrified Plating");

        PetrifiedPlating plating = suspendCard();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(plating);
        assertThat(gd.exiledCardTimeCounters).containsEntry(plating.getId(), 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void upkeepRemovesOneCounterOnlyWhenTriggerResolves() {
        PetrifiedPlating plating = suspendCard();

        advanceToUpkeep(player1);

        assertThat(gd.exiledCardTimeCounters).containsEntry(plating.getId(), 2);
        harness.passBothPriorities();
        assertThat(gd.exiledCardTimeCounters).containsEntry(plating.getId(), 1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(plating);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void cannotSuspendDuringUpkeepWithoutFlashPermission() {
        harness.setHand(player1, List.of(new PetrifiedPlating()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void auraDoesNotResolveWhenItsTargetLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NessianCourser());
        harness.setHand(player1, List.of(new PetrifiedPlating()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, creature.getId());

        harness.setHand(player2, List.of(new VenserShaperSavant()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Petrified Plating");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(creature.getCard());
    }

    private PetrifiedPlating suspendCard() {
        PetrifiedPlating plating = new PetrifiedPlating();
        harness.setHand(player1, List.of(plating));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateHandAbility(player1, 0, null);
        return plating;
    }
}
