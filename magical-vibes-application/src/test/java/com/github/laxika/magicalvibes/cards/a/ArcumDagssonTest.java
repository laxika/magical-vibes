package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BorealGriffin;
import com.github.laxika.magicalvibes.cards.m.MishrasBauble;
import com.github.laxika.magicalvibes.cards.p.PhyrexianIronfoot;
import com.github.laxika.magicalvibes.cards.t.TajuruPreserver;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArcumDagsson.class, BorealGriffin.class, MishrasBauble.class, PhyrexianIronfoot.class, TajuruPreserver.class})
class ArcumDagssonTest extends BaseCardTest {
    @Test
    @DisplayName("Searching an empty library still completes after sacrificing the creature")
    void searchingEmptyLibraryCompletes() {
        addCreatureReady(player1, new ArcumDagsson());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PhyrexianIronfoot());
        harness.setLibrary(player2, List.of());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Phyrexian Ironfoot");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
    @Test
    @DisplayName("Sacrifice protection preserves the target but still permits its controller to search")
    void protectedOpponentStillMaySearch() {
        addCreatureReady(player1, new ArcumDagsson());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PhyrexianIronfoot());
        harness.addToBattlefield(player2, new TajuruPreserver());
        harness.setLibrary(player2, List.of(new MishrasBauble()));

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Phyrexian Ironfoot");
        harness.assertNotInGraveyard(player2, "Phyrexian Ironfoot");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        harness.assertOnBattlefield(player2, "Phyrexian Ironfoot");
        harness.assertOnBattlefield(player2, "Mishra's Bauble");
    }

    @Test
    @DisplayName("Can sacrifice your own artifact creature and search your own library")
    void sacrificesOwnCreatureAndSearchesOwnLibrary() {
        Permanent arcum = addCreatureReady(player1, new ArcumDagsson());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PhyrexianIronfoot());
        harness.setLibrary(player1, List.of(new MishrasBauble()));

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(arcum.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Phyrexian Ironfoot");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Phyrexian Ironfoot");
        harness.assertOnBattlefield(player1, "Mishra's Bauble");
        assertThat(findPermanent(player1, "Mishra's Bauble").isTapped()).isFalse();
    }

    @Test
    @DisplayName("May fail to find even when a noncreature artifact is available")
    void mayFailToFind() {
        addCreatureReady(player1, new ArcumDagsson());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PhyrexianIronfoot());
        harness.setLibrary(player2, List.of(new MishrasBauble()));

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, -1);

        harness.assertInGraveyard(player2, "Phyrexian Ironfoot");
        harness.assertNotOnBattlefield(player2, "Mishra's Bauble");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new ArcumDagsson());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PhyrexianIronfoot());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent arcum = addCreatureReady(player1, new ArcumDagsson());
        arcum.setTapped(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PhyrexianIronfoot());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a nonartifact creature")
    void cannotTargetNonartifactCreature() {
        addCreatureReady(player1, new ArcumDagsson());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BorealGriffin());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature artifact")
    void cannotTargetNoncreatureArtifact() {
        addCreatureReady(player1, new ArcumDagsson());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MishrasBauble());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponent sacrifices the targeted artifact creature and may search their own library")
    void opponentSacrificesAndSearchesOwnLibrary() {
        addCreatureReady(player1, new ArcumDagsson());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PhyrexianIronfoot());
        harness.setLibrary(player2, List.of(new BorealGriffin(), new MishrasBauble(), new PhyrexianIronfoot()));

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Phyrexian Ironfoot");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .allMatch(card -> card.hasType(CardType.ARTIFACT))
                .noneMatch(card -> card.hasType(CardType.CREATURE));

        harness.handleCardChosen(player2, 0);

        harness.assertOnBattlefield(player2, "Mishra's Bauble");
        harness.assertNotOnBattlefield(player1, "Mishra's Bauble");
    }

    @Test
    @DisplayName("Declining the search leaves the sacrificed creature in the graveyard")
    void decliningSearchDoesNotSearch() {
        addCreatureReady(player1, new ArcumDagsson());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PhyrexianIronfoot());
        harness.setLibrary(player2, List.of(new MishrasBauble()));

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Phyrexian Ironfoot");
        harness.assertNotOnBattlefield(player2, "Mishra's Bauble");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }
}
