package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CasualtiesOfWar.class, Spellbook.class, GrizzlyBears.class, GloriousAnthem.class,
        Forest.class, JaceBeleren.class, Ornithopter.class})
class CasualtiesOfWarTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the selected artifact, creature, enchantment, land, and planeswalker")
    void destroysAllSelectedPermanentTypes() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());

        cast(new int[]{0, 1, 2, 3, 4}, List.of(
                artifact.getId(), creature.getId(), enchantment.getId(), land.getId(), planeswalker.getId()));

        harness.assertNotOnBattlefield(player2, "Spellbook");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertNotOnBattlefield(player2, "Jace Beleren");
    }

    @Test
    @DisplayName("Allows the same artifact creature to be chosen for both matching modes")
    void allowsSharedArtifactCreatureTarget() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        cast(new int[]{0, 1}, List.of(artifactCreature.getId(), artifactCreature.getId()));

        harness.assertNotOnBattlefield(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Rejects a target that does not match the selected mode")
    void rejectsMismatchedTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CasualtiesOfWar()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 5, new int[]{0}, List.of(creature.getId()), null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, List<java.util.UUID> targets) {
        harness.setHand(player1, List.of(new CasualtiesOfWar()));
        addMana();
        harness.castModalSorceryWithModes(player1, 0, 1, 5, modes, targets, null);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
