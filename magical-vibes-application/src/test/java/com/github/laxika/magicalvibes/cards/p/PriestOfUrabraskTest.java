package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GutShot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PriestOfUrabrask.class, GutShot.class})
class PriestOfUrabraskTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Priest of Urabrask puts it on the stack as a creature spell")
    void castingPutsOnStack() {
        castPriestOfUrabrask();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Priest of Urabrask");
    }

    // ===== Resolving creature spell =====

    @Test
    @DisplayName("Resolving puts Priest of Urabrask on battlefield with ETB trigger on stack")
    void resolvingPutsOnBattlefieldWithEtbOnStack() {
        castPriestOfUrabrask();
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Priest of Urabrask");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Priest of Urabrask");
    }

    // ===== ETB mana production =====

    @Test
    @DisplayName("ETB trigger adds three red mana to controller's mana pool")
    void etbAddsThreeRedMana() {
        castPriestOfUrabrask();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterResolution() {
        castPriestOfUrabrask();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.stack).isEmpty();
    }

    // ===== Helpers =====

    private void castPriestOfUrabrask() {
        harness.castFromHand(player1, new PriestOfUrabrask(), "{2}{R}");
    }

    @Test
    @DisplayName("Entering without being cast adds mana to the entering creature's controller")
    void enteringWithoutCastingAddsManaToController() {
        harness.enterBattlefieldAndReturn(player2, new PriestOfUrabrask());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("ETB trigger still adds mana after Priest of Urabrask dies in response")
    void triggerResolvesAfterSourceDies() {
        castPriestOfUrabrask();
        harness.passBothPriorities();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();

        harness.setHand(player2, List.of(new GutShot()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Priest of Urabrask"));

        harness.assertNotOnBattlefield(player1, "Priest of Urabrask");
        harness.assertInGraveyard(player1, "Priest of Urabrask");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
