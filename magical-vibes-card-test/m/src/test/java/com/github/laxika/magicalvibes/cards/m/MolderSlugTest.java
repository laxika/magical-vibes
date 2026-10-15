package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.t.TelJiladArchers;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.cards.t.TreeOfTales;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MolderSlug.class, Bonesplitter.class, LeoninScimitar.class, TelJiladArchers.class,
        MyrEnforcer.class, TreeOfTales.class, Terror.class})
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

    @Test
    @DisplayName("Only the active player sacrifices even when both players control artifacts")
    void onlyActivePlayerSacrifices() {
        harness.addToBattlefield(player1, new MolderSlug());
        harness.addToBattlefield(player1, new Bonesplitter());
        harness.addToBattlefield(player2, new LeoninScimitar());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bonesplitter");
        harness.assertOnBattlefield(player1, "Molder Slug");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("An active player with no artifacts does not sacrifice another player's artifact")
    void noArtifactsDoesNotAffectOtherPlayer() {
        harness.addToBattlefield(player1, new MolderSlug());
        harness.addToBattlefield(player1, new Bonesplitter());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bonesplitter");
        harness.assertOnBattlefield(player1, "Molder Slug");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Artifact creatures can be sacrificed")
    void sacrificesArtifactCreature() {
        harness.addToBattlefield(player1, new MolderSlug());
        harness.addToBattlefield(player2, new MyrEnforcer());
        harness.addToBattlefield(player2, new TelJiladArchers());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Myr Enforcer");
        harness.assertInGraveyard(player2, "Myr Enforcer");
        harness.assertOnBattlefield(player2, "Tel-Jilad Archers");
    }

    @Test
    @DisplayName("Artifact lands can be sacrificed")
    void sacrificesArtifactLand() {
        harness.addToBattlefield(player1, new MolderSlug());
        harness.addToBattlefield(player2, new TreeOfTales());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Tree of Tales");
        harness.assertInGraveyard(player2, "Tree of Tales");
    }

    @Test
    @DisplayName("Removing Molder Slug in response does not stop its upkeep trigger")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        Permanent slug = harness.addToBattlefieldAndReturn(player1, new MolderSlug());
        harness.addToBattlefield(player2, new Bonesplitter());

        advanceToUpkeep(player2);
        harness.setHand(player2, List.of(new Terror()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, slug.getId());

        harness.assertNotOnBattlefield(player1, "Molder Slug");
        harness.assertInGraveyard(player1, "Molder Slug");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Bonesplitter");
        harness.assertInGraveyard(player2, "Bonesplitter");
    }
}
