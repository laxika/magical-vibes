package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Cloudpost;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Glimmerpost;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.t.TheDrossPits;
import com.github.laxika.magicalvibes.cards.t.TheHunterMaze;
import com.github.laxika.magicalvibes.cards.t.TheMycosynthGardens;
import com.github.laxika.magicalvibes.cards.t.TheSeedcore;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MonumentToPerfection.class, Cloudpost.class, Forest.class, Glimmerpost.class,
        Island.class, Mountain.class, Plains.class, Swamp.class, TheMycosynthGardens.class,
        TheSeedcore.class, TheDrossPits.class, TheHunterMaze.class})
class MonumentToPerfectionTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability searches for a basic, Sphere, or Locus land")
    void searchesForBasicSphereOrLocusLand() {
        harness.addToBattlefield(player1, new MonumentToPerfection());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        Glimmerpost glimmerpost = new Glimmerpost();
        harness.setLibrary(player1, List.of(new Forest(), glimmerpost, new TheSeedcore(),
                new Cloudpost(), new MonumentToPerfection()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Glimmerpost", "The Seedcore", "Cloudpost");
        assertThat(search.params().reveals()).isTrue();

        harness.handleCardChosen(player1, search.params().cards().indexOf(glimmerpost));

        harness.assertInHand(player1, "Glimmerpost");
    }

    @Test
    @DisplayName("The second ability needs nine differently named eligible lands")
    void animationRequiresNineDistinctEligibleLandNames() {
        harness.addToBattlefield(player1, new MonumentToPerfection());
        addEligibleLands(new Forest(), new Island(), new Mountain(), new Plains(), new Swamp(),
                new Cloudpost(), new Glimmerpost(), new TheSeedcore(), new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different names");
    }

    @Test
    @DisplayName("The second ability animates Monument to Perfection indefinitely")
    void animationMakesMonumentA9By9IndestructibleToxicCreature() {
        Permanent monument = harness.addToBattlefieldAndReturn(player1, new MonumentToPerfection());
        addEligibleLands(new Forest(), new Island(), new Mountain(), new Plains(), new Swamp(),
                new Cloudpost(), new Glimmerpost(), new TheSeedcore(), new TheMycosynthGardens());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, monument)).isTrue();
        assertThat(gqs.isArtifact(monument)).isTrue();
        assertThat(gqs.getEffectivePower(gd, monument)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, monument)).isEqualTo(9);
        assertThat(gqs.effectiveCreatureSubtypes(gd, monument))
                .containsExactlyInAnyOrder(CardSubtype.PHYREXIAN, CardSubtype.CONSTRUCT);
        assertThat(gqs.hasKeyword(gd, monument, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, monument, Keyword.TOXIC)).isTrue();
        assertThat(gs.getEffectiveActivatedAbilities(gd, monument)).isEmpty();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, monument)).isTrue();
        assertThat(gqs.hasKeyword(gd, monument, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, monument, Keyword.TOXIC)).isTrue();
        assertThat(gs.getEffectiveActivatedAbilities(gd, monument)).isEmpty();
    }

    @Test
    @DisplayName("An animated Monument gives nine poison counters in addition to combat damage")
    void animatedMonumentDealsNinePoisonCounters() {
        Permanent monument = harness.addToBattlefieldAndReturn(player1, new MonumentToPerfection());
        addNineOneLands();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        monument.setSummoningSick(false);
        monument.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 11);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(9);
    }

    @Test
    @DisplayName("Multiple queued animation activations still grant only toxic nine")
    void queuedAnimationsDoNotStackToxic() {
        Permanent monument = harness.addToBattlefieldAndReturn(player1, new MonumentToPerfection());
        addNineOneLands();
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.activateAbility(player1, 0, 1, null, null);
        resolveAllTriggers();

        monument.setSummoningSick(false);
        monument.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 11);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(9);
    }

    @Test
    @DisplayName("Opposing eligible lands do not satisfy the activation restriction")
    void opposingLandsDoNotCount() {
        harness.addToBattlefield(player1, new MonumentToPerfection());
        addEligibleLands(new Forest(), new Island(), new Mountain(), new Plains(), new Swamp(),
                new TheSeedcore(), new TheMycosynthGardens(), new TheDrossPits());
        harness.addToBattlefield(player2, new TheHunterMaze());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different names");
    }

    @Test
    @DisplayName("The eligible land count is checked only when activating")
    void animationResolvesAfterAnEligibleLandLeaves() {
        Permanent monument = harness.addToBattlefieldAndReturn(player1, new MonumentToPerfection());
        addNineOneLands();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 1, null, null);
        gd.playerBattlefields.get(player1.getId()).removeLast();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, monument)).isTrue();
        assertThat(gqs.getEffectivePower(gd, monument)).isEqualTo(9);
        assertThat(gs.getEffectiveActivatedAbilities(gd, monument)).isEmpty();
    }

    @Test
    @DisplayName("The restricted search may fail to find even when a basic land is available")
    void searchMayFailToFind() {
        Permanent monument = harness.addToBattlefieldAndReturn(player1, new MonumentToPerfection());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().canFailToFind()).isTrue();
        harness.handleCardChosen(player1, -1);

        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(monument.isTapped()).isTrue();
    }

    private void addNineOneLands() {
        addEligibleLands(new Forest(), new Island(), new Mountain(), new Plains(), new Swamp(),
                new TheSeedcore(), new TheMycosynthGardens(), new TheDrossPits(), new TheHunterMaze());
    }

    private void addEligibleLands(Card... lands) {
        for (Card land : lands) {
            harness.addToBattlefield(player1, land);
        }
    }
}
