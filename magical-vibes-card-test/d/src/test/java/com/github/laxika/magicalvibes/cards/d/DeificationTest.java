package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Deification.class, JaceBeleren.class, GarrukWildspeaker.class,
        GrizzlyBears.class, LightningBolt.class})
class DeificationTest extends BaseCardTest {

    @Test
    void choosesAPlaneswalkerType() {
        harness.castFromHand(player1, new Deification(), "{1}{W}");
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.prompt()).isEqualTo("Choose a planeswalker type.");
        assertThat(choice.options()).contains("JACE", "GARRUK").doesNotContain("GOBLIN");

        harness.handleListChoice(player1, "JACE");

        assertThat(findPermanent(player1, "Deification").getChosenSubtype()).isEqualTo(CardSubtype.JACE);
    }

    @Test
    void grantsHexproofOnlyToOwnPlaneswalkersOfChosenType() {
        Permanent deification = harness.addToBattlefieldAndReturn(player1, new Deification());
        deification.setChosenSubtype(CardSubtype.JACE);
        Permanent ownJace = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        Permanent ownGarruk = harness.addToBattlefieldAndReturn(player1, new GarrukWildspeaker());
        Permanent opponentJace = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());

        assertThat(gqs.hasKeyword(gd, ownJace, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownGarruk, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentJace, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void preservesOneLoyaltyWhenControllingACreature() {
        Permanent deification = harness.addToBattlefieldAndReturn(player1, new Deification());
        deification.setChosenSubtype(CardSubtype.JACE);
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 3);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, jace.getId());

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.damageDealtToPermanentsThisTurn.get(jace.getId())).isEqualTo(3);
    }

    @Test
    void doesNotPreserveLoyaltyWithoutAControlledCreature() {
        Permanent deification = harness.addToBattlefieldAndReturn(player1, new Deification());
        deification.setChosenSubtype(CardSubtype.JACE);
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 3);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, jace.getId());

        harness.assertInGraveyard(player1, "Jace Beleren");
    }

    @Test
    void opponentCannotTargetChosenPlaneswalkerEvenWithoutACreature() {
        Permanent deification = harness.addToBattlefieldAndReturn(player1, new Deification());
        deification.setChosenSubtype(CardSubtype.JACE);
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, jace.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    void nonlethalDamageRemovesTheFullNumberOfCounters() {
        Permanent deification = harness.addToBattlefieldAndReturn(player1, new Deification());
        deification.setChosenSubtype(CardSubtype.JACE);
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, jace.getId());

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Jace Beleren");
    }

    @Test
    void repeatedDamageAtOneLoyaltyStillDealsDamageWithoutRemovingTheLastCounter() {
        Permanent deification = harness.addToBattlefieldAndReturn(player1, new Deification());
        deification.setChosenSubtype(CardSubtype.JACE);
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 2);
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, jace.getId());
        harness.castAndResolveInstant(player1, 0, jace.getId());

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.damageDealtToPermanentsThisTurn.get(jace.getId())).isEqualTo(6);
        harness.assertOnBattlefield(player1, "Jace Beleren");
    }

    @Test
    void anOpponentsCreatureDoesNotEnableLoyaltyProtection() {
        Permanent deification = harness.addToBattlefieldAndReturn(player1, new Deification());
        deification.setChosenSubtype(CardSubtype.JACE);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, jace.getId());

        harness.assertInGraveyard(player1, "Jace Beleren");
    }

    @Test
    void losingTheLastCreatureInResponseRemovesLoyaltyProtection() {
        Permanent deification = harness.addToBattlefieldAndReturn(player1, new Deification());
        deification.setChosenSubtype(CardSubtype.JACE);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, jace.getId());
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Jace Beleren");
    }

    @Test
    void doesNotProtectAPlaneswalkerOfAnotherTypeFromDamage() {
        Permanent deification = harness.addToBattlefieldAndReturn(player1, new Deification());
        deification.setChosenSubtype(CardSubtype.JACE);
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent garruk = harness.addToBattlefieldAndReturn(player1, new GarrukWildspeaker());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, garruk.getId());

        harness.assertInGraveyard(player1, "Garruk Wildspeaker");
    }

    @Test
    void doesNotProtectAnOpponentsPlaneswalkerFromDamage() {
        Permanent deification = harness.addToBattlefieldAndReturn(player1, new Deification());
        deification.setChosenSubtype(CardSubtype.JACE);
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, jace.getId());

        harness.assertInGraveyard(player2, "Jace Beleren");
    }

    @Test
    void loyaltyAbilityCostsCanRemoveTheLastCounter() {
        Permanent deification = harness.addToBattlefieldAndReturn(player1, new Deification());
        deification.setChosenSubtype(CardSubtype.JACE);
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 2, 1, null, player1.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Jace Beleren");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void hexproofDoesNotStopCombatDamageAndLethalCombatDamageLeavesOneCounter() {
        Permanent deification = harness.addToBattlefieldAndReturn(player1, new Deification());
        deification.setChosenSubtype(CardSubtype.JACE);
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 2);
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(jace.getId());
        harness.forceActivePlayer(player2);

        harness.resolveCombatDamage();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.damageDealtToPermanentsThisTurn.get(jace.getId())).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Jace Beleren");
    }
}
