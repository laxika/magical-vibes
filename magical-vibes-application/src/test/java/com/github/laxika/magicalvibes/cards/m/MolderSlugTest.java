package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.t.TelJiladArchers;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MolderSlug.class, Bonesplitter.class, LeoninScimitar.class, TelJiladArchers.class})
class MolderSlugTest extends BaseCardTest {

    @Test
    @DisplayName("At a player's upkeep that player sacrifices an artifact")
    void sacrificesArtifactAtThatPlayersUpkeep() {
        harness.addToBattlefield(player1, new MolderSlug());
        harness.addToBattlefield(player2, new Bonesplitter());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Bonesplitter");
        harness.assertInGraveyard(player2, "Bonesplitter");
    }

    @Test
    @DisplayName("The controller sacrifices an artifact at their own upkeep")
    void sacrificesArtifactAtControllerUpkeep() {
        harness.addToBattlefield(player1, new MolderSlug());
        harness.addToBattlefield(player1, new LeoninScimitar());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Leonin Scimitar");
    }

    @Test
    @DisplayName("A non-artifact permanent is not sacrificed")
    void nonArtifactPermanentNotSacrificed() {
        harness.addToBattlefield(player1, new MolderSlug());
        harness.addToBattlefield(player2, new TelJiladArchers());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Tel-Jilad Archers");
    }

    @Test
    @DisplayName("With multiple artifacts the player chooses which one to sacrifice")
    void choosesAmongArtifacts() {
        harness.addToBattlefield(player1, new MolderSlug());
        harness.addToBattlefield(player2, new Bonesplitter());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultiplePermanentsChosen(player2, List.of(scimitar.getId()));

        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        harness.assertOnBattlefield(player2, "Bonesplitter");
    }
}
