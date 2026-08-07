package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HearthKamiTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target artifact with mana value X and sacrifices itself")
    void destroysArtifactWithManaValueX() {
        harness.addToBattlefield(player1, new HearthKami());
        harness.addToBattlefield(player2, new RodOfRuin()); // mana value 4
        UUID target = harness.getPermanentId(player2, "Rod of Ruin");

        harness.addMana(player1, ManaColor.RED, 4);
        harness.activateAbility(player1, 0, 4, target);
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
        harness.addToBattlefield(player2, new BottleGnomes()); // artifact creature, mana value 3
        UUID target = harness.getPermanentId(player2, "Bottle Gnomes");

        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, 0, 3, target);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Bottle Gnomes");
    }

    @Test
    @DisplayName("Cannot target an artifact whose mana value does not equal X")
    void cannotTargetArtifactWithDifferentManaValue() {
        harness.addToBattlefield(player1, new HearthKami());
        harness.addToBattlefield(player2, new RodOfRuin()); // mana value 4
        UUID target = harness.getPermanentId(player2, "Rod of Ruin");

        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, target))
                .isInstanceOf(IllegalStateException.class);
    }
}
