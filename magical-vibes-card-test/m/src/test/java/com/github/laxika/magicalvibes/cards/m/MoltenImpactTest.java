package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.t.TezzeretBetrayerOfFlesh;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({MoltenImpact.class, GrizzlyBears.class, HillGiant.class, LightningBolt.class,
        AirElemental.class, TezzeretBetrayerOfFlesh.class})
class MoltenImpactTest extends BaseCardTest {

    @Test
    @DisplayName("Excess damage makes a one-time spell boon that deals the noted amount")
    void excessDamageCreatesOneShotSpellBoonWithTheNotedAmount() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent boonTarget = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        harness.setHand(player1, List.of(new MoltenImpact()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, firstTarget.getId());

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(boonTarget.getId()).doesNotContain(ownCreature.getId());
        harness.handlePermanentChosen(player1, boonTarget.getId());
        resolveAllTriggers();

        assertThat(boonTarget.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Exact damage does not create the boon")
    void exactDamageDoesNotCreateTheBoon() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent unaffectedTarget = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.setHand(player1, List.of(new MoltenImpact()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, firstTarget.getId());

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(unaffectedTarget.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Boon is consumed after one instant and does not trigger for opponent spells")
    void boonIsConsumedAfterOneInstantAndDoesNotTriggerForOpponentSpells() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent boonTarget = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new MoltenImpact()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, firstTarget.getId());

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        assertThat(boonTarget.getMarkedDamage()).isZero();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, boonTarget.getId());
        resolveAllTriggers();
        assertThat(boonTarget.getMarkedDamage()).isEqualTo(2);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(boonTarget.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Initial spell can target own creature and boon triggers for sorcery")
    void initialSpellCanTargetOwnCreatureAndBoonTriggersForSorcery() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent boonTarget = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new MoltenImpact()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, firstTarget.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new MoltenImpact()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, secondTarget.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, boonTarget.getId());
        resolveAllTriggers();

        assertThat(boonTarget.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Damage already marked is included when calculating excess")
    void damageAlreadyMarkedIsIncludedWhenCalculatingExcess() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent boonTarget = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, firstTarget.getId());

        harness.setHand(player1, List.of(new MoltenImpact()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, firstTarget.getId());

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, boonTarget.getId());
        resolveAllTriggers();
        assertThat(boonTarget.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Excess damage to planeswalker creates boon that can damage another planeswalker")
    void excessDamageToPlaneswalkerCreatesBoonThatCanDamageAnotherPlaneswalker() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new TezzeretBetrayerOfFlesh());
        firstTarget.setCounterCount(CounterType.LOYALTY, 2);
        harness.setHand(player1, List.of(new MoltenImpact()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, firstTarget.getId());
        harness.assertInGraveyard(player2, "Tezzeret, Betrayer of Flesh");
        Permanent boonTarget = harness.addToBattlefieldAndReturn(player2, new TezzeretBetrayerOfFlesh());
        boonTarget.setCounterCount(CounterType.LOYALTY, 4);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, boonTarget.getId());
        resolveAllTriggers();
        assertThat(boonTarget.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Boon survives turn cleanup and the original card leaving the graveyard")
    void boonSurvivesTurnCleanupAndTheOriginalCardLeavingTheGraveyard() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent boonTarget = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new MoltenImpact()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, firstTarget.getId());
        gd.playerGraveyards.get(player1.getId()).clear();
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, boonTarget.getId());
        resolveAllTriggers();
        assertThat(boonTarget.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Illegal initial target does not create boon")
    void illegalInitialTargetDoesNotCreateBoon() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent unaffectedTarget = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new MoltenImpact()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, firstTarget.getId());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, firstTarget.getId());
        resolveAllTriggers();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(unaffectedTarget.getMarkedDamage()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
