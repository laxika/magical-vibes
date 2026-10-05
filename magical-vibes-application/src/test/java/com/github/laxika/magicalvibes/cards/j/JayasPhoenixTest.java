package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ChandraNovicePyromancer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JayasPhoenix.class, ChandraNovicePyromancer.class, JaceBeleren.class, AirElemental.class, GrizzlyBears.class})
class JayasPhoenixTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage makes the next loyalty ability resolve twice")
    void copiesNextLoyaltyAbility() {
        addCreatureReady(player1, new JayasPhoenix());
        Permanent chandra = addReadyChandra(player1, 5);
        Permanent elemental = addCreatureReady(player1, new AirElemental());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.pendingNextLoyaltyAbilityCopyThisTurnCount.get(player1.getId())).isEqualTo(1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(chandra), 0, null, null);
        resolveAllTriggers();

        assertThat(elemental.getPowerModifier()).isEqualTo(4);
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("Casting a planeswalker lets the Phoenix return from the graveyard")
    void returnsFromGraveyardOnPlaneswalkerCast() {
        JayasPhoenix phoenix = new JayasPhoenix();
        harness.setGraveyard(player1, List.of(phoenix));
        harness.setHand(player1, List.of(new JaceBeleren()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(phoenix.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(phoenix.getId()));
    }

    @Test
    @DisplayName("Casting a non-planeswalker spell does not trigger the graveyard ability")
    void nonPlaneswalkerCastDoesNotTrigger() {
        JayasPhoenix phoenix = new JayasPhoenix();
        harness.setGraveyard(player1, List.of(phoenix));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(phoenix.getId()));
    }

    @Test
    void mayDeclineGraveyardReturn() {
        harness.setGraveyard(player1, List.of(new JayasPhoenix()));
        harness.setHand(player1, List.of(new ChandraNovicePyromancer()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castPlaneswalker(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Jaya's Phoenix");
        harness.assertNotOnBattlefield(player1, "Jaya's Phoenix");
        harness.assertOnBattlefield(player1, "Chandra, Novice Pyromancer");
    }

    @Test
    void opponentsPlaneswalkerDoesNotTriggerReturn() {
        harness.setGraveyard(player1, List.of(new JayasPhoenix()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new ChandraNovicePyromancer()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castPlaneswalker(player2, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Jaya's Phoenix");
        harness.assertNotOnBattlefield(player1, "Jaya's Phoenix");
    }

    @Test
    void combatDamageToPlaneswalkerEnablesCopy() {
        addCreatureReady(player1, new JayasPhoenix());
        Permanent chandra = addReadyChandra(player1, 5);
        Permanent elemental = addCreatureReady(player1, new AirElemental());
        Permanent defendingChandra = addReadyChandra(player2, 5);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, defendingChandra.getId()));
        resolveCombat();
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(chandra), 0, null, null);
        resolveAllTriggers();

        assertThat(defendingChandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(elemental.getPowerModifier()).isEqualTo(4);
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    void copyMayChooseNewTargetWithoutPayingLoyaltyAgain() {
        addCreatureReady(player1, new JayasPhoenix());
        Permanent chandra = addReadyChandra(player1, 5);
        Permanent elemental = addCreatureReady(player1, new AirElemental());
        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(chandra),
                2, null, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, elemental.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 15);
        assertThat(elemental.getMarkedDamage()).isEqualTo(2);
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void multiplePhoenixesCreateSeparateDelayedTriggers() {
        addCreatureReady(player1, new JayasPhoenix());
        addCreatureReady(player1, new JayasPhoenix());
        Permanent chandra = addReadyChandra(player1, 5);
        declareAttackers(List.of(0, 1));
        resolveCombat();
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(chandra), 0, null, null);

        assertThat(gd.stack).hasSize(3);
    }

    @Test
    void onlyNextLoyaltyAbilityIsCopied() {
        addCreatureReady(player1, new JayasPhoenix());
        Permanent chandra = addReadyChandra(player1, 5);
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(chandra), 1, null, null);
        resolveAllTriggers();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(4);

        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(jace), 1, null, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void delayedCopyTriggerKeepsPhoenixAsItsSource() {
        Permanent phoenix = addCreatureReady(player1, new JayasPhoenix());
        Permanent chandra = addReadyChandra(player1, 5);
        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(chandra), 0, null, null);

        assertThat(gd.stack).filteredOn(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .singleElement().satisfies(entry -> {
                    assertThat(entry.getCard().getId()).isEqualTo(phoenix.getCard().getId());
                    assertThat(entry.getSourcePermanentId()).isEqualTo(phoenix.getId());
                });
    }

    @Test
    void oldReturnTriggerCannotReturnPhoenixThatLeftAndReenteredGraveyard() {
        JayasPhoenix phoenix = new JayasPhoenix();
        harness.setGraveyard(player1, List.of(phoenix));
        harness.setHand(player1, List.of(new ChandraNovicePyromancer()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castPlaneswalker(player1, 0);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removeCardFromGraveyardById(gd, phoenix.getId()));
        Permanent returnedPhoenix = harness.enterBattlefieldAndReturn(player1, phoenix);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, returnedPhoenix));

        resolveAllTriggers();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Jaya's Phoenix");
        harness.assertNotOnBattlefield(player1, "Jaya's Phoenix");
    }

    private Permanent addReadyChandra(Player player, int loyalty) {
        Permanent chandra = harness.addToBattlefieldAndReturn(player, new ChandraNovicePyromancer());
        chandra.setCounterCount(CounterType.LOYALTY, loyalty);
        chandra.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return chandra;
    }
}
