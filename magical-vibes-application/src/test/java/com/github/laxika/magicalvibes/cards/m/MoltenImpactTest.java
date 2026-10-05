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
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoltenImpact.class, GrizzlyBears.class, HillGiant.class, LightningBolt.class,
        AirElemental.class, TezzeretBetrayerOfFlesh.class})
class MoltenImpactTest extends BaseCardTest {

    @Test
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
