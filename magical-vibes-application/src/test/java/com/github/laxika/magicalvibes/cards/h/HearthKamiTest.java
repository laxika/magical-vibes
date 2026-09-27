package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HearthKami.class, RodOfRuin.class, BottleGnomes.class, Ornithopter.class})
class HearthKamiTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target artifact with mana value X and sacrifices itself")
    void destroysArtifactWithManaValueX() {
        harness.addToBattlefield(player1, new HearthKami());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RodOfRuin()); // mana value 4

        harness.addMana(player1, ManaColor.RED, 4);
        harness.activateAbility(player1, 0, 4, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Rod of Ruin");
        harness.assertInGraveyard(player2, "Rod of Ruin");
        harness.assertNotOnBattlefield(player1, "Hearth Kami");
        harness.assertInGraveyard(player1, "Hearth Kami");
    }

    @Test
    @DisplayName("Can destroy an artifact creature with mana value X")
    void destroysArtifactCreature() {
        harness.addToBattlefield(player1, new HearthKami());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BottleGnomes()); // artifact creature, mana value 3

        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, 0, 3, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Bottle Gnomes");
    }

    @Test
    @DisplayName("Cannot target an artifact whose mana value does not equal X")
    void cannotTargetArtifactWithDifferentManaValue() {
        harness.addToBattlefield(player1, new HearthKami());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RodOfRuin()); // mana value 4

        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-artifact permanent")
    void cannotTargetNonArtifact() {
        harness.addToBattlefield(player1, new HearthKami());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HearthKami());

        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can choose X equal to zero for a zero-mana artifact")
    void destroysZeroManaValueArtifact() {
        harness.addToBattlefield(player1, new HearthKami());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        harness.activateAbility(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
        harness.assertInGraveyard(player1, "Hearth Kami");
    }
}
