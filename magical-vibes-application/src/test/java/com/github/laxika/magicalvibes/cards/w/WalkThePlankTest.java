package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WalkThePlankTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Walk the Plank targeting a non-Merfolk creature puts it on stack")
    void castingPutsOnStack() {
        Permanent bears = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new WalkThePlank()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, bears.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Walk the Plank");
        assertThat(entry.getTargetId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Cannot target a Merfolk creature")
    void cannotTargetMerfolkCreature() {
        // Add a non-Merfolk creature as valid target so spell is playable
        harness.getGameData().playerBattlefields.get(player1.getId()).add(new Permanent(new GrizzlyBears()));

        Permanent merfolk = new Permanent(new CoralMerfolk());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(merfolk);

        harness.setHand(player1, List.of(new WalkThePlank()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, merfolk.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Merfolk");
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving Walk the Plank destroys target creature")
    void resolvingDestroysTargetCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new WalkThePlank()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Walk the Plank");
    }

    @Test
    @DisplayName("Walk the Plank allows regeneration")
    void allowsRegeneration() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setRegenerationShield(1);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new WalkThePlank()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Walk the Plank fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bears = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new WalkThePlank()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, bears.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Walk the Plank");
    }
}
