package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GlistenerElf;
import com.github.laxika.magicalvibes.cards.p.PorcelainLegionnaire;
import com.github.laxika.magicalvibes.cards.s.ShrineOfBoundlessGrowth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarReport.class, GlistenerElf.class, ShrineOfBoundlessGrowth.class, PorcelainLegionnaire.class})
class WarReportTest extends BaseCardTest {

    @Test
    @DisplayName("Casting War Report puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new WarReport()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("War Report");
    }

    @Test
    @DisplayName("Gains life equal to creature count plus artifact count")
    void gainsLifeForCreaturesAndArtifacts() {
        // 2 creatures + 1 artifact = 3 life
        addCreature(player1);
        addCreature(player2);
        addArtifact(player1);

        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new WarReport()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Artifact creatures are counted twice — once as creature, once as artifact")
    void artifactCreatureCountedTwice() {
        // 1 artifact creature counts as both creature and artifact = 2 life
        addArtifactCreature(player1);

        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new WarReport()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Counts permanents from all players' battlefields")
    void countsBothPlayersBattlefields() {
        // Player 1: 1 creature, 1 artifact. Player 2: 2 creatures, 1 artifact = 5 life
        addCreature(player1);
        addArtifact(player1);
        addCreature(player2);
        addCreature(player2);
        addArtifact(player2);

        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new WarReport()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Gains no life when no creatures or artifacts on the battlefield")
    void gainsNoLifeWhenEmpty() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new WarReport()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Only creatures count — gains life with only creatures on battlefield")
    void gainsLifeWithOnlyCreatures() {
        addCreature(player1);
        addCreature(player1);

        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new WarReport()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Only artifacts count — gains life with only artifacts on battlefield")
    void gainsLifeWithOnlyArtifacts() {
        addArtifact(player1);
        addArtifact(player2);

        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new WarReport()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Counts permanents that enter after War Report is cast")
    void countsPermanentsAtResolution() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 17);
        harness.setHand(player1, List.of(new WarReport()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0);
        addCreature(player2);
        addArtifactCreature(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Does not count permanents that leave before resolution")
    void excludesPermanentsThatLeftBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GlistenerElf());
        addArtifactCreature(player1);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new WarReport()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0);
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.setGraveyard(player2, List.of(creature.getCard()));
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Does not count creature or artifact cards outside the battlefield")
    void excludesCardsInOtherZones() {
        harness.setHand(player2, List.of(new GlistenerElf(), new PorcelainLegionnaire()));
        harness.setGraveyard(player1, List.of(new PorcelainLegionnaire()));
        harness.setExile(player2, List.of(new ShrineOfBoundlessGrowth()));
        harness.setLibrary(player1, List.of(new GlistenerElf(), new ShrineOfBoundlessGrowth()));
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new WarReport()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player1, 20);
    }

    private void addCreature(Player player) {
        harness.addToBattlefield(player, new GlistenerElf());
    }

    private void addArtifact(Player player) {
        harness.addToBattlefield(player, new ShrineOfBoundlessGrowth());
    }

    private void addArtifactCreature(Player player) {
        harness.addToBattlefield(player, new PorcelainLegionnaire());
    }
}
