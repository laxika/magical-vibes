package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BontusMonumentTest extends BaseCardTest {

    // ===== Cost reduction =====

    @Test
    @DisplayName("Black creature spells cost {1} less")
    void blackCreatureSpellsCostOneLess() {
        harness.addToBattlefield(player1, new BontusMonument());
        // Walking Corpse costs {1}{B} — with the {1} reduction it should cost just {B}
        harness.setHand(player1, List.of(new WalkingCorpse()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(e -> e.getCard().getName().equals("Walking Corpse"));
    }

    @Test
    @DisplayName("Cannot cast a black creature without enough mana even with the reduction")
    void cannotCastBlackCreatureWithoutEnoughMana() {
        harness.addToBattlefield(player1, new BontusMonument());
        // Walking Corpse costs {1}{B} — with {1} reduction needs {B}; no mana is not enough
        harness.setHand(player1, List.of(new WalkingCorpse()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-black creature spells are not reduced")
    void nonBlackCreaturesNotReduced() {
        harness.addToBattlefield(player1, new BontusMonument());
        // Grizzly Bears costs {1}{G} — not black, so only {G} is not enough
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Creature-cast drain trigger =====

    @Test
    @DisplayName("Casting a creature drains each opponent 1 and gains the controller 1 life")
    void castingCreatureDrainsAndGainsLife() {
        harness.addToBattlefield(player1, new BontusMonument());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        int p1Before = gd.playerLifeTotals.get(player1.getId());
        int p2Before = gd.playerLifeTotals.get(player2.getId());

        harness.castCreature(player1, 0);
        // Trigger is on top of the stack (LIFO) — resolve it
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2Before - 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1Before + 1);
    }

    @Test
    @DisplayName("Casting a creature puts the drain trigger on the stack")
    void castingCreatureTriggers() {
        harness.addToBattlefield(player1, new BontusMonument());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Bontu's Monument"));
    }

    // ===== Non-creature spell does not trigger =====

    @Test
    @DisplayName("Casting a non-creature spell does not trigger the drain")
    void nonCreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new BontusMonument());
        // Spellbook is a {2} artifact — not a creature spell
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);

        // Only the artifact spell on the stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }
}
