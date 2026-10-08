package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.v.VineMare;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StarCrownedStag.class, GreenwoodSentinel.class, Plains.class, Murder.class,
        InvasionOfZendikar.class, AwakenedSkyclave.class, VineMare.class})
class StarCrownedStagTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking taps a chosen creature the defending player controls")
    void tapsDefendingCreature() {
        addCreatureReady(player1, new StarCrownedStag());
        Permanent victim = addCreatureReady(player2, new GreenwoodSentinel());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only creatures the defending player controls are legal targets")
    void ownCreaturesAreNotLegalTargets() {
        Permanent attacker = addCreatureReady(player1, new StarCrownedStag());
        Permanent ownCreature = addCreatureReady(player1, new GreenwoodSentinel());
        Permanent victim = addCreatureReady(player2, new GreenwoodSentinel());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactly(victim.getId())
                .doesNotContain(ownCreature.getId(), attacker.getId());
    }

    @Test
    @DisplayName("No target selection when the defending player controls no creature")
    void noTargetWithoutDefendingCreature() {
        addCreatureReady(player1, new StarCrownedStag());

        declareAttackers(player1, List.of(0));

        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
    }

    @Test
    @DisplayName("Defending player's noncreature permanents cannot be targeted")
    void noncreaturePermanentsAreNotLegalTargets() {
        addCreatureReady(player1, new StarCrownedStag());
        Permanent victim = addCreatureReady(player2, new GreenwoodSentinel());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactly(victim.getId())
                .doesNotContain(land.getId());
    }

    @Test
    @DisplayName("Defending creatures with hexproof cannot be targeted")
    void hexproofCreaturesAreNotLegalTargets() {
        addCreatureReady(player1, new StarCrownedStag());
        Permanent victim = addCreatureReady(player2, new GreenwoodSentinel());
        Permanent hexproofCreature = addCreatureReady(player2, new VineMare());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactly(victim.getId())
                .doesNotContain(hexproofCreature.getId());
    }

    @Test
    @DisplayName("An already tapped defending creature remains a legal target")
    void canTargetTappedCreature() {
        addCreatureReady(player1, new StarCrownedStag());
        Permanent victim = addCreatureReady(player2, new GreenwoodSentinel());
        victim.tap();

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactly(victim.getId());
        harness.handlePermanentChosen(player1, victim.getId());
        resolveAllTriggers();

        assertThat(victim.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Another creature attacking does not trigger a nonattacking Stag")
    void doesNotTriggerForAnotherAttacker() {
        addCreatureReady(player1, new StarCrownedStag());
        addCreatureReady(player1, new GreenwoodSentinel());
        Permanent victim = addCreatureReady(player2, new GreenwoodSentinel());

        declareAttackers(List.of(1));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(victim.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The attack trigger resolves even if the Stag is destroyed in response")
    void triggerSurvivesSourceRemoval() {
        Permanent attacker = addCreatureReady(player1, new StarCrownedStag());
        Permanent victim = addCreatureReady(player2, new GreenwoodSentinel());
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.castAndResolveInstant(player2, 0, attacker.getId());
        harness.assertInGraveyard(player1, "Star-Crowned Stag");
        resolveAllTriggers();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A destroyed target does not cause the attack trigger to tap another creature")
    void doesNotRetargetWhenTargetIsDestroyed() {
        addCreatureReady(player1, new StarCrownedStag());
        Permanent victim = addCreatureReady(player2, new GreenwoodSentinel());
        Permanent otherCreature = addCreatureReady(player2, new StarCrownedStag());
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.castAndResolveInstant(player1, 0, victim.getId());
        harness.assertInGraveyard(player2, "Greenwood Sentinel");
        resolveAllTriggers();

        assertThat(otherCreature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Attacking a battle taps a creature controlled by its protector")
    void targetsBattleProtectorsCreature() {
        addCreatureReady(player1, new StarCrownedStag());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfZendikar());
        battle.setProtectorPlayerId(player2.getId());
        battle.setCounterCount(CounterType.DEFENSE, 3);
        Permanent victim = addCreatureReady(player2, new GreenwoodSentinel());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, battle.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactly(victim.getId());
        harness.handlePermanentChosen(player1, victim.getId());
        resolveAllTriggers();

        assertThat(victim.isTapped()).isTrue();
    }
}
