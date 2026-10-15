package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NecrogenScudder.class})
class NecrogenScudderTest extends BaseCardTest {

    

    @Test
    @DisplayName("Casting Necrogen Scudder puts it on stack as creature spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new NecrogenScudder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Necrogen Scudder");
    }

    @Test
    @DisplayName("Resolving creature spell puts ETB trigger on stack")
    void resolvingCreaturePutsEtbOnStack() {
        harness.setHand(player1, List.of(new NecrogenScudder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Necrogen Scudder");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Necrogen Scudder");
    }

    @Test
    @DisplayName("ETB causes controller to lose 3 life")
    void etbLoses3Life() {
        harness.setHand(player1, List.of(new NecrogenScudder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Entering without being cast makes the entering controller lose life only on resolution")
    void enteringWithoutCastingLosesLifeOnResolution() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player2, new NecrogenScudder());

        harness.assertOnBattlefield(player2, "Necrogen Scudder");
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Each Necrogen Scudder entry creates a separate mandatory life-loss trigger")
    void multipleEntriesLoseLifeSeparately() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player1, new NecrogenScudder());
        harness.enterBattlefieldAndReturn(player1, new NecrogenScudder());

        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 17);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 14);
        harness.assertLife(player2, 20);
    }
}
