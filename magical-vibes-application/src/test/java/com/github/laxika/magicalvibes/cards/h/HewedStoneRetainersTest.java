package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.Refocus;
import com.github.laxika.magicalvibes.cards.r.RenownedWeaponsmith;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HewedStoneRetainers.class, GrizzlyBears.class, Refocus.class, RenownedWeaponsmith.class})
class HewedStoneRetainersTest extends BaseCardTest {

    @Test
    @DisplayName("Castable after another spell was cast this turn")
    void castableAfterAnotherSpell() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new HewedStoneRetainers()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0); // Grizzly Bears
        harness.passBothPriorities();

        harness.castCreature(player1, 0); // Hewed Stone Retainers

        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hewed Stone Retainers");
    }

    @Test
    @DisplayName("Not castable when no other spell was cast this turn")
    void notCastableWithoutAnotherSpell() {
        harness.setHand(player1, List.of(new HewedStoneRetainers()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void castableAfterNoncreatureSpell() {
        var creature = harness.addToBattlefieldAndReturn(player1, new RenownedWeaponsmith());
        harness.setHand(player1, List.of(new Refocus(), new HewedStoneRetainers()));
        harness.setLibrary(player1, List.of(new RenownedWeaponsmith()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hewed Stone Retainers");
    }

    @Test
    void opponentsSpellDoesNotSatisfyRestriction() {
        var creature = harness.addToBattlefieldAndReturn(player1, new RenownedWeaponsmith());
        harness.setHand(player1, List.of(new HewedStoneRetainers()));
        harness.setHand(player2, List.of(new Refocus()));
        harness.setLibrary(player2, List.of(new RenownedWeaponsmith()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void spellCastOnPreviousTurnDoesNotSatisfyRestriction() {
        harness.setHand(player1, List.of(new RenownedWeaponsmith(), new HewedStoneRetainers()));
        harness.setLibrary(player1, List.of(new RenownedWeaponsmith(), new RenownedWeaponsmith()));
        harness.setLibrary(player2, List.of(new RenownedWeaponsmith(), new RenownedWeaponsmith()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void puttingCreatureOntoBattlefieldDoesNotCountAsCasting() {
        harness.addToBattlefield(player1, new RenownedWeaponsmith());
        harness.setHand(player1, List.of(new HewedStoneRetainers()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
