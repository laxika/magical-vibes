package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BorealGriffin;
import com.github.laxika.magicalvibes.cards.m.MishrasBauble;
import com.github.laxika.magicalvibes.cards.p.PhyrexianIronfoot;
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

@CardUsed({ArcumDagsson.class, BorealGriffin.class, MishrasBauble.class, PhyrexianIronfoot.class})
class ArcumDagssonTest extends BaseCardTest {

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
