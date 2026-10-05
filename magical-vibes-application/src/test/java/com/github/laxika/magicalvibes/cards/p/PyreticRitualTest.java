package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PyreticRitual.class})
class PyreticRitualTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Pyretic Ritual puts it on the stack as an instant spell")
    void castingPutsOnStack() {
        preparePyreticRitual();
        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(PyreticRitual.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Resolving adds three red mana to controller's mana pool")
    void resolvingAddsThreeRedMana() {
        preparePyreticRitual();
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        preparePyreticRitual();
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Pyretic Ritual");
    }

    @Test
    @DisplayName("Produced mana is added to the mana remaining after paying the spell cost")
    void addsToRemainingMana() {
        harness.setHand(player1, List.of(new PyreticRitual()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0);
        int remainingRed = gd.playerManaPools.get(player1.getId()).get(ManaColor.RED);
        int remainingGreen = gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN);

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(remainingRed + 3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(remainingGreen);
    }

    @Test
    @DisplayName("The nonactive player receives the mana when casting Pyretic Ritual")
    void nonactiveControllerReceivesMana() {
        harness.setHand(player2, List.of(new PyreticRitual()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        gs.passPriority(gd, player1);

        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.assertInGraveyard(player2, "Pyretic Ritual");
    }

    private void preparePyreticRitual() {
        harness.setHand(player1, List.of(new PyreticRitual()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
