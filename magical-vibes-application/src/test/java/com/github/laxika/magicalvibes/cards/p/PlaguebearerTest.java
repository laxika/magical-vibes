package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlaguebearerTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a nonblack creature with mana value X")
    void destroysNonblackCreatureWithManaValueX() {
        harness.addToBattlefield(player1, new Plaguebearer());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID target = harness.getPermanentId(player2, "Grizzly Bears");
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.activateAbility(player1, 0, 2, target);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature whose mana value is not X")
    void cannotTargetCreatureWithDifferentManaValue() {
        harness.addToBattlefield(player1, new Plaguebearer());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID target = harness.getPermanentId(player2, "Grizzly Bears");
        harness.addMana(player1, ManaColor.BLACK, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        harness.addToBattlefield(player1, new Plaguebearer());
        harness.addToBattlefield(player2, new BlackKnight());
        UUID target = harness.getPermanentId(player2, "Black Knight");
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, target))
                .isInstanceOf(IllegalStateException.class);
    }
}
