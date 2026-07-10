package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GaddockTeegTest extends BaseCardTest {

    @Test
    @DisplayName("Noncreature spell with mana value 4 or greater can't be cast")
    void blocksHighManaValueNoncreatureSpell() {
        harness.addToBattlefield(player1, new GaddockTeeg());
        harness.setHand(player1, List.of(new WrathOfGod())); // {2}{W}{W} sorcery, MV 4
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Noncreature spell with {X} in its cost can't be cast even at low mana value")
    void blocksXNoncreatureSpell() {
        harness.addToBattlefield(player1, new GaddockTeeg());
        harness.setHand(player1, List.of(new Hurricane())); // {X}{G} sorcery — MV 1 at X=0, but has {X}
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Noncreature spell with mana value 3 or less is still castable")
    void allowsLowManaValueNoncreatureSpell() {
        harness.addToBattlefield(player1, new GaddockTeeg());
        harness.setHand(player1, List.of(new Shock())); // {R} instant, MV 1
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("Creature spell with mana value 4 or greater is still castable")
    void allowsHighManaValueCreatureSpell() {
        harness.addToBattlefield(player1, new GaddockTeeg());
        harness.setHand(player1, List.of(new HillGiant())); // {3}{R} creature, MV 4
        harness.addMana(player1, ManaColor.RED, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Restriction is symmetric — the opponent also can't cast high-MV noncreature spells")
    void restrictionAppliesToOpponent() {
        harness.addToBattlefield(player1, new GaddockTeeg());
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Removing Gaddock Teeg restores casting of high-MV noncreature spells")
    void removingGaddockTeegRestoresCasting() {
        harness.addToBattlefield(player1, new GaddockTeeg());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Gaddock Teeg"));

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
    }
}
