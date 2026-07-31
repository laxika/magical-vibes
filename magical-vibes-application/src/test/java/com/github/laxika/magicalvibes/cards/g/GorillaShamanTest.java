package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GorillaShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target noncreature artifact with mana value X")
    void destroysNoncreatureArtifactWithManaValueX() {
        harness.addToBattlefield(player1, new GorillaShaman());
        harness.addToBattlefield(player2, new RodOfRuin()); // mana value 4
        UUID target = harness.getPermanentId(player2, "Rod of Ruin");

        harness.addMana(player1, ManaColor.RED, 9); // {X=4}{X=4}{1}
        harness.activateAbility(player1, 0, 4, target);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Rod of Ruin");
        harness.assertInGraveyard(player2, "Rod of Ruin");
    }

    @Test
    @DisplayName("Cannot target an artifact whose mana value does not equal X")
    void cannotTargetArtifactWithDifferentManaValue() {
        harness.addToBattlefield(player1, new GorillaShaman());
        harness.addToBattlefield(player2, new RodOfRuin()); // mana value 4
        UUID target = harness.getPermanentId(player2, "Rod of Ruin");

        harness.addMana(player1, ManaColor.RED, 7); // {X=3}{X=3}{1}

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        harness.addToBattlefield(player1, new GorillaShaman());
        harness.addToBattlefield(player2, new BottleGnomes()); // artifact creature, mana value 3
        UUID target = harness.getPermanentId(player2, "Bottle Gnomes");

        harness.addMana(player1, ManaColor.RED, 7); // {X=3}{X=3}{1}

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, target))
                .isInstanceOf(IllegalStateException.class);
    }
}
