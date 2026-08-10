package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MolderSlugTest extends BaseCardTest {

    @Test
    @DisplayName("At a player's upkeep that player sacrifices an artifact")
    void sacrificesArtifactAtThatPlayersUpkeep() {
        harness.addToBattlefield(player1, new MolderSlug());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(artifact.getId()));
        harness.assertInGraveyard(player2, "Spellbook");
    }

    @Test
    @DisplayName("The controller sacrifices an artifact at their own upkeep")
    void sacrificesArtifactAtControllerUpkeep() {
        harness.addToBattlefield(player1, new MolderSlug());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(artifact.getId()));
    }

    @Test
    @DisplayName("A non-artifact permanent is not sacrificed")
    void nonArtifactPermanentNotSacrificed() {
        harness.addToBattlefield(player1, new MolderSlug());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("With multiple artifacts the player chooses which one to sacrifice")
    void choosesAmongArtifacts() {
        harness.addToBattlefield(player1, new MolderSlug());
        Permanent spellbook = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultiplePermanentsChosen(player2, List.of(fountain.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(fountain.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(spellbook.getId()));
    }
}
