package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArchmagesCharm.class, Shock.class, Island.class, Ornithopter.class, GrizzlyBears.class})
class ArchmagesCharmTest extends BaseCardTest {

    @Test
    void countersTargetSpell() {
        harness.forceActivePlayer(player2);
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.setHand(player1, List.of(new ArchmagesCharm()));
        addBlueMana();

        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 0, 0, shock.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void targetPlayerDrawsTwoCards() {
        Island first = new Island();
        Island second = new Island();
        harness.setLibrary(player2, List.of(first, second));
        harness.setHand(player1, List.of(new ArchmagesCharm()));
        addBlueMana();

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.castInstant(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 2)
                .contains(first, second);
    }

    @Test
    void gainsPermanentControlOfLowManaValueNonland() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, List.of(new ArchmagesCharm()));
        addBlueMana();

        harness.castInstant(player1, 0, 2, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player2, "Ornithopter");
    }

    @Test
    void cannotGainControlOfLandOrHigherManaValuePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new ArchmagesCharm()));
        addBlueMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland");

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value");
    }

    @Test
    void drawModeCannotTargetAPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, List.of(new ArchmagesCharm()));
        addBlueMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addBlueMana() {
        harness.addMana(player1, ManaColor.BLUE, 3);
    }
}
