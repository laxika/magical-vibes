package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.ShieldSphere;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrimitiveJustice.class, GrizzlyBears.class, Ornithopter.class, ShieldSphere.class})
class PrimitiveJusticeTest extends BaseCardTest {

    @Test
    @DisplayName("An illegal additional target does not prevent life gain while the base target remains legal")
    void gainsLifeWhenAdditionalTargetLeavesBattlefield() {
        Permanent baseTarget = harness.addToBattlefieldAndReturn(player2, new ShieldSphere());
        Permanent additionalTarget = harness.addToBattlefieldAndReturn(player2, new ShieldSphere());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new PrimitiveJustice()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorceryWithRepeatedCosts(player1, 0, List.of("{1}{G}"),
                List.of(baseTarget.getId(), additionalTarget.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(additionalTarget);
        gd.playerHands.get(player2.getId()).add(additionalTarget.getCard());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Shield Sphere");
        harness.assertInGraveyard(player2, "Shield Sphere");
        harness.assertInHand(player2, "Shield Sphere");
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("No life is gained when every artifact target becomes illegal")
    void gainsNoLifeWhenAllTargetsLeaveBattlefield() {
        Permanent baseTarget = harness.addToBattlefieldAndReturn(player2, new ShieldSphere());
        Permanent additionalTarget = harness.addToBattlefieldAndReturn(player2, new ShieldSphere());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new PrimitiveJustice()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorceryWithRepeatedCosts(player1, 0, List.of("{1}{G}"),
                List.of(baseTarget.getId(), additionalTarget.getId()));
        gd.playerBattlefields.get(player2.getId()).removeAll(List.of(baseTarget, additionalTarget));
        gd.playerHands.get(player2.getId()).addAll(List.of(baseTarget.getCard(), additionalTarget.getCard()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Shield Sphere");
        harness.assertInGraveyard(player1, "Primitive Justice");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Artifacts controlled by the caster are legal targets")
    void canDestroyOwnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ShieldSphere());
        harness.setHand(player1, List.of(new PrimitiveJustice()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorceryWithRepeatedCosts(player1, 0, List.of(), List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Shield Sphere");
        harness.assertInGraveyard(player1, "Shield Sphere");
    }

    @Test
    @DisplayName("With no additional payment, destroys a single target artifact and gains no life")
    void destroysOneArtifactWithoutAdditionalPayments() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new PrimitiveJustice()));
        harness.addMana(player1, ManaColor.RED, 2); // {1}{R}

        harness.castSorceryWithRepeatedCosts(player1, 0, List.of(), List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Ornithopter");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Each additional {1}{R} paid destroys another target artifact but gains no life")
    void redPaymentAddsTargetWithoutLife() {
        Permanent a1 = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent a2 = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new PrimitiveJustice()));
        harness.addMana(player1, ManaColor.RED, 4); // {1}{R} + {1}{R}

        harness.castSorceryWithRepeatedCosts(player1, 0, List.of("{1}{R}"),
                List.of(a1.getId(), a2.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Each additional {1}{G} paid destroys another target artifact and gains 1 life")
    void greenPaymentAddsTargetAndLife() {
        Permanent a1 = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent a2 = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent a3 = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new PrimitiveJustice()));
        harness.addMana(player1, ManaColor.RED, 2); // {1}{R}
        harness.addMana(player1, ManaColor.GREEN, 4); // {1}{G} twice

        harness.castSorceryWithRepeatedCosts(player1, 0, List.of("{1}{G}", "{1}{G}"),
                List.of(a1.getId(), a2.getId(), a3.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Mixed {1}{R} and {1}{G} payments gain life only for the green ones")
    void mixedPaymentsGainLifePerGreenPayment() {
        Permanent a1 = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent a2 = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent a3 = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new PrimitiveJustice()));
        harness.addMana(player1, ManaColor.RED, 4); // {1}{R} + additional {1}{R}
        harness.addMana(player1, ManaColor.GREEN, 2); // additional {1}{G}

        harness.castSorceryWithRepeatedCosts(player1, 0, List.of("{1}{R}", "{1}{G}"),
                List.of(a1.getId(), a2.getId(), a3.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Cannot target more artifacts than the payments made bought")
    void cannotTargetMoreArtifactsThanPaidFor() {
        Permanent a1 = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent a2 = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, List.of(new PrimitiveJustice()));
        harness.addMana(player1, ManaColor.RED, 4);

        List<UUID> twoTargets = List.of(a1.getId(), a2.getId());
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, twoTargets))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Must choose the base artifact target even without additional payments")
    void requiresBaseArtifactTarget() {
        harness.setHand(player1, List.of(new PrimitiveJustice()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorceryWithRepeatedCosts(player1, 0, List.of(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Must choose one additional artifact target for each additional payment")
    void requiresTargetForEachAdditionalPayment() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, List.of(new PrimitiveJustice()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorceryWithRepeatedCosts(player1, 0, List.of("{1}{R}"),
                List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target the same artifact more than once")
    void cannotTargetSameArtifactMoreThanOnce() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, List.of(new PrimitiveJustice()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorceryWithRepeatedCosts(player1, 0, List.of("{1}{R}"),
                List.of(artifact.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The additional cost must be paid to cast with extra targets")
    void additionalPaymentMustBeAffordable() {
        Permanent a1 = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent a2 = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, List.of(new PrimitiveJustice()));
        harness.addMana(player1, ManaColor.RED, 2); // only enough for the base cost

        List<UUID> twoTargets = List.of(a1.getId(), a2.getId());
        assertThatThrownBy(() -> harness.castSorceryWithRepeatedCosts(player1, 0, List.of("{1}{R}"), twoTargets))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a nonartifact permanent")
    void cannotTargetNonArtifact() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PrimitiveJustice()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID bearsId = bears.getId();
        assertThatThrownBy(() -> harness.castSorceryWithRepeatedCosts(player1, 0, List.of(), List.of(bearsId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifacts");
    }
}
