package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.StarfieldShepherd;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.t.TezzeretCruelCaptain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlpharaelStonechosen.class, GrizzlyBears.class, Shock.class, StarfieldShepherd.class, Swamp.class,
        TezzeretCruelCaptain.class})
class AlpharaelStonechosenTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking without a Void event only deals combat damage")
    void attackingWithoutVoidEventOnlyDealsCombatDamage() {
        addReadyAlpharael();
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Void triggers when a nonland permanent left the battlefield")
    void voidTriggersAfterNonlandPermanentLeavesBattlefield() {
        addReadyAlpharael();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bears));
        harness.setLife(player2, 19);

        declareAttackers(List.of(0));
        resolveAllTriggers();
        resolveCombat();

        harness.assertLife(player2, 6);
    }

    @Test
    @DisplayName("Void does not trigger when only a land left the battlefield")
    void voidDoesNotTriggerAfterLandLeavesBattlefield() {
        addReadyAlpharael();
        Permanent swamp = harness.addToBattlefieldAndReturn(player2, new Swamp());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, swamp));
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Void triggers after a spell was cast for its Warp cost")
    void voidTriggersAfterWarpSpell() {
        addReadyAlpharael();
        StarfieldShepherd shepherd = new StarfieldShepherd();
        harness.setHand(player2, List.of(shepherd));
        harness.setLibrary(player2, List.of());
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreatureWithAlternateCost(player2, 0, List.of());
        harness.passBothPriorities();
        harness.setLife(player2, 19);

        declareAttackers(List.of(0));
        resolveAllTriggers();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        harness.assertLife(player2, 6);
    }

    @Test
    @DisplayName("Ward discards a random card without opening a card-choice interaction")
    void wardDiscardsRandomCard() {
        Permanent alpharael = addReadyAlpharael();
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(shock, bears));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, alpharael.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Ward counters the targeted spell when its controller has no card")
    void wardCountersWhenHandIsEmptyAfterCasting() {
        Permanent alpharael = addReadyAlpharael();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, alpharael.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(alpharael.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Ward counters the spell when its controller declines to discard")
    void wardCountersWhenDiscardIsDeclined() {
        Permanent alpharael = addReadyAlpharael();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock(), new Swamp()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, alpharael.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInHand(player2, "Swamp");
        harness.assertInGraveyard(player2, "Shock");
        assertThat(alpharael.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Ward does not trigger for the controller's own spell")
    void wardDoesNotTriggerForOwnSpell() {
        Permanent alpharael = addReadyAlpharael();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock(), new Swamp()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, alpharael.getId());
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Swamp");
        assertThat(alpharael.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ward counters an opponent's activated ability when they cannot discard")
    void wardCountersOpponentsActivatedAbility() {
        Permanent alpharael = addReadyAlpharael();
        alpharael.tap();
        Permanent tezzeret = harness.addToBattlefieldAndReturn(player2, new TezzeretCruelCaptain());
        tezzeret.setCounterCount(CounterType.LOYALTY, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of());

        harness.activateAbility(player2, 0, 0, null, alpharael.getId());
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, this::resolveAllTriggers);

        assertThat(alpharael.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyAlpharael() {
        return addCreatureReady(player1, new AlpharaelStonechosen());
    }

    @Test
    @DisplayName("Void uses the defending player's life total at resolution")
    void voidUsesLifeTotalAtResolution() {
        addReadyAlpharael();
        Permanent shepherd = harness.addToBattlefieldAndReturn(player1, new StarfieldShepherd());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, shepherd));
        harness.setLife(player2, 19);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        harness.setLife(player2, 24);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        harness.assertLife(player2, 12);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Void still resolves after Alpharael leaves combat and the battlefield")
    void voidResolvesAfterSourceLeavesBattlefield() {
        Permanent alpharael = addReadyAlpharael();
        Permanent shepherd = harness.addToBattlefieldAndReturn(player1, new StarfieldShepherd());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, shepherd));
        harness.setLife(player2, 19);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, alpharael));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        harness.assertLife(player2, 9);
    }

    @Test
    @DisplayName("Void makes the defending player lose life when attacking a planeswalker")
    void voidWhenAttackingPlaneswalker() {
        attackPlaneswalker(false);
    }

    @Test
    @DisplayName("Void still makes the defending player lose life after the attacked planeswalker leaves")
    void voidWhenAttackedPlaneswalkerLeavesBeforeResolution() {
        attackPlaneswalker(true);
    }

    private void attackPlaneswalker(boolean removeBeforeResolution) {
        addReadyAlpharael();
        Permanent shepherd = harness.addToBattlefieldAndReturn(player1, new StarfieldShepherd());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, shepherd));
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new TezzeretCruelCaptain());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        harness.setLife(player2, 19);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId())));
        assertThat(gd.stack).hasSize(1);
        if (removeBeforeResolution) {
            harness.inMutationScope(() -> harness.getPermanentRemovalService()
                    .removePermanentToGraveyard(gd, planeswalker));
        }
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        harness.assertLife(player2, 9);
    }
}
