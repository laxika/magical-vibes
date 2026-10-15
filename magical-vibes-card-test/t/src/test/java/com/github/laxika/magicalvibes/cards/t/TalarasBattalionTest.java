package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.s.SlipperyBogle;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TalarasBattalion.class, GrizzlyBears.class, SuntailHawk.class, SlipperyBogle.class})
class TalarasBattalionTest extends BaseCardTest {

    @Test
    @DisplayName("Castable after another green spell was cast this turn")
    void castableAfterGreenSpell() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new TalarasBattalion()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0); // Grizzly Bears (green)
        harness.passBothPriorities(); // resolve it, stack empties

        harness.castCreature(player1, 0); // Talara's Battalion

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Talara's Battalion");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Talara's Battalion");
    }

    @Test
    @DisplayName("Not castable when no other spell was cast this turn")
    void notCastableWithoutAnotherSpell() {
        harness.setHand(player1, List.of(new TalarasBattalion()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Not castable when only a non-green spell was cast this turn")
    void notCastableAfterNonGreenSpell() {
        harness.setHand(player1, List.of(new SuntailHawk(), new TalarasBattalion()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0); // Suntail Hawk (white)
        harness.passBothPriorities(); // resolve it, stack empties

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("A green hybrid spell qualifies even when paid entirely with blue mana")
    void castableAfterHybridSpellPaidWithBlue() {
        harness.setHand(player1, List.of(new SlipperyBogle(), new TalarasBattalion()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Talara's Battalion");
    }

    @Test
    @DisplayName("Entering a green permanent without casting it does not qualify")
    void greenPermanentWithoutCastingDoesNotQualify() {
        harness.addToBattlefield(player1, new SlipperyBogle());
        harness.setHand(player1, List.of(new TalarasBattalion()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("A green spell from the previous turn does not qualify")
    void previousTurnGreenSpellDoesNotQualify() {
        harness.setHand(player1, List.of(new SlipperyBogle(), new TalarasBattalion()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Trample deals excess damage to the defending player")
    void trampleDealsExcessDamage() {
        addCreatureReady(player1, new TalarasBattalion());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SlipperyBogle());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 3
        ));

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Slippery Bogle");
        harness.assertOnBattlefield(player1, "Talara's Battalion");
    }
}
