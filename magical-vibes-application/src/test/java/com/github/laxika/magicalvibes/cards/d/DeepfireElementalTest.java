package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeepfireElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target artifact with mana value X")
    void destroysArtifactWithManaValueX() {
        harness.addToBattlefield(player1, new DeepfireElemental());
        harness.addToBattlefield(player2, new RodOfRuin());
        UUID target = harness.getPermanentId(player2, "Rod of Ruin");
        harness.addMana(player1, ManaColor.RED, 9);

        harness.activateAbility(player1, 0, 4, target);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Rod of Ruin");
        harness.assertInGraveyard(player2, "Rod of Ruin");
    }

    @Test
    @DisplayName("Destroys a target creature with mana value X")
    void destroysCreatureWithManaValueX() {
        harness.addToBattlefield(player1, new DeepfireElemental());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID target = harness.getPermanentId(player2, "Grizzly Bears");
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateAbility(player1, 0, 2, target);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can destroy an artifact creature with mana value X")
    void destroysArtifactCreatureWithManaValueX() {
        harness.addToBattlefield(player1, new DeepfireElemental());
        harness.addToBattlefield(player2, new BottleGnomes());
        UUID target = harness.getPermanentId(player2, "Bottle Gnomes");
        harness.addMana(player1, ManaColor.RED, 7);

        harness.activateAbility(player1, 0, 3, target);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Bottle Gnomes");
        harness.assertInGraveyard(player2, "Bottle Gnomes");
    }

    @Test
    @DisplayName("Cannot target a permanent whose mana value differs from X")
    void cannotTargetPermanentWithDifferentManaValue() {
        harness.addToBattlefield(player1, new DeepfireElemental());
        harness.addToBattlefield(player2, new RodOfRuin());
        UUID target = harness.getPermanentId(player2, "Rod of Ruin");
        harness.addMana(player1, ManaColor.RED, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a permanent that is neither an artifact nor a creature")
    void cannotTargetNonArtifactNonCreature() {
        harness.addToBattlefield(player1, new DeepfireElemental());
        harness.addToBattlefield(player2, new Forest());
        UUID target = harness.getPermanentId(player2, "Forest");
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, target))
                .isInstanceOf(IllegalStateException.class);
    }
}
