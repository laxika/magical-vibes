package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.l.LazotepPlating;
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
        Forest.class, JaceBeleren.class, Ornithopter.class, LazotepPlating.class})
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

    @Test
    @DisplayName("Choosing only the land mode leaves unselected permanent types intact")
    void choosesOnlyLandMode() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast(new int[]{3}, List.of(land.getId()));

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        harness.assertOnBattlefield(player2, "Spellbook");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can destroy a permanent controlled by the caster")
    void destroysOwnLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        cast(new int[]{3}, List.of(land.getId()));

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Resolves the legal land mode when the artifact target gains hexproof")
    void resolvesRemainingLegalTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new CasualtiesOfWar()));
        addMana();
        harness.castModalSorceryWithModes(player1, 0, 1, 5,
                new int[]{0, 3}, List.of(artifact.getId(), land.getId()), null);

        harness.castFromHand(player2, new LazotepPlating(), "{1}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Spellbook");
        harness.assertNotInGraveyard(player2, "Spellbook");
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Casualties of War");
    }

    @Test
    @DisplayName("Does not destroy anything when every selected target gains hexproof")
    void allTargetsBecomeIllegal() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new CasualtiesOfWar()));
        addMana();
        harness.castModalSorceryWithModes(player1, 0, 1, 5,
                new int[]{0, 3}, List.of(artifact.getId(), land.getId()), null);

        harness.castFromHand(player2, new LazotepPlating(), "{1}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Spellbook");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player1, "Casualties of War");
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
