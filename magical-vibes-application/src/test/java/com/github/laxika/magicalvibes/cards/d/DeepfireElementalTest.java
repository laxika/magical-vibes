package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeepfireElemental.class, RodOfRuin.class, GrizzlyBears.class,
        BottleGnomes.class, Forest.class, Ornithopter.class})
class DeepfireElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target artifact with mana value X")
    void destroysArtifactWithManaValueX() {
        harness.addToBattlefield(player1, new DeepfireElemental());
        Permanent targetPermanent = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        harness.addMana(player1, ManaColor.RED, 9);

        harness.activateAbility(player1, 0, 4, targetPermanent.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Rod of Ruin");
        harness.assertInGraveyard(player2, "Rod of Ruin");
    }

    @Test
    @DisplayName("Destroys a target creature with mana value X")
    void destroysCreatureWithManaValueX() {
        harness.addToBattlefield(player1, new DeepfireElemental());
        Permanent targetPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateAbility(player1, 0, 2, targetPermanent.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can destroy an artifact creature with mana value X")
    void destroysArtifactCreatureWithManaValueX() {
        harness.addToBattlefield(player1, new DeepfireElemental());
        Permanent targetPermanent = harness.addToBattlefieldAndReturn(player2, new BottleGnomes());
        harness.addMana(player1, ManaColor.RED, 7);

        harness.activateAbility(player1, 0, 3, targetPermanent.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Bottle Gnomes");
        harness.assertInGraveyard(player2, "Bottle Gnomes");
    }

    @Test
    @DisplayName("Can choose X equal to zero for a zero-mana artifact creature")
    void destroysZeroManaValueArtifactCreatureWithXZero() {
        harness.addToBattlefield(player1, new DeepfireElemental());
        Permanent targetPermanent = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, targetPermanent.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Requires two payments of X plus one generic mana")
    void requiresTwoPaymentsOfX() {
        harness.addToBattlefield(player1, new DeepfireElemental());
        Permanent targetPermanent = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        harness.addMana(player1, ManaColor.RED, 8);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 4, targetPermanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a permanent whose mana value differs from X")
    void cannotTargetPermanentWithDifferentManaValue() {
        harness.addToBattlefield(player1, new DeepfireElemental());
        Permanent targetPermanent = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        harness.addMana(player1, ManaColor.RED, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, targetPermanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a permanent that is neither an artifact nor a creature")
    void cannotTargetNonArtifactNonCreature() {
        harness.addToBattlefield(player1, new DeepfireElemental());
        Permanent targetPermanent = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, targetPermanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target itself without tapping or waiting for summoning sickness to end")
    void canDestroyItself() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new DeepfireElemental());
        source.setSummoningSick(true);
        source.setTapped(true);
        harness.addMana(player1, ManaColor.RED, 13);

        harness.activateAbility(player1, 0, 6, source.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Deepfire Elemental");
        harness.assertInGraveyard(player1, "Deepfire Elemental");
    }

    @Test
    @DisplayName("Repeated activations retain their independently chosen X values")
    void repeatedActivationsKeepSeparateXValues() {
        harness.addToBattlefield(player1, new DeepfireElemental());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 14);

        harness.activateAbility(player1, 0, 4, artifact.getId());
        harness.activateAbility(player1, 0, 2, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Rod of Ruin");

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Rod of Ruin");
        harness.assertOnBattlefield(player1, "Deepfire Elemental");
    }

    @Test
    @DisplayName("An activated ability still resolves after Deepfire Elemental is destroyed")
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new DeepfireElemental());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        harness.addMana(player1, ManaColor.RED, 22);

        harness.activateAbility(player1, 0, 4, artifact.getId());
        harness.activateAbility(player1, 0, 6, source.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Deepfire Elemental");
        harness.assertOnBattlefield(player2, "Rod of Ruin");

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Rod of Ruin");
    }
}
