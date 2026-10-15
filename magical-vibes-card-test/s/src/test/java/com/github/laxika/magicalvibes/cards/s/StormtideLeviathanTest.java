package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.m.MaritimeGuard;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StormtideLeviathan.class, Forest.class, Mountain.class, MaritimeGuard.class})
class StormtideLeviathanTest extends BaseCardTest {

    @Test
    @DisplayName("All lands gain Island subtype when Stormtide Leviathan is on the battlefield")
    void allLandsAreIslands() {
        harness.addToBattlefield(player1, new StormtideLeviathan());

        // Add a non-Island land (Forest)
        Card forest = new Card();
        forest.setName("Forest");
        forest.setType(CardType.LAND);
        forest.setSubtypes(List.of(CardSubtype.FOREST));
        Permanent forestPerm = new Permanent(forest);
        gd.playerBattlefields.get(player1.getId()).add(forestPerm);

        // Add a Mountain for opponent
        Card mountain = new Card();
        mountain.setName("Mountain");
        mountain.setType(CardType.LAND);
        mountain.setSubtypes(List.of(CardSubtype.MOUNTAIN));
        Permanent mountainPerm = new Permanent(mountain);
        gd.playerBattlefields.get(player2.getId()).add(mountainPerm);

        // Static effects grant Island subtype to all lands (computed on-the-fly)
        GameQueryService.StaticBonus forestBonus = gqs.computeStaticBonus(gd, forestPerm);
        assertThat(forestBonus.grantedSubtypes()).contains(CardSubtype.ISLAND);

        GameQueryService.StaticBonus mountainBonus = gqs.computeStaticBonus(gd, mountainPerm);
        assertThat(mountainBonus.grantedSubtypes()).contains(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("Non-land permanents do not gain Island subtype")
    void nonLandsDontGetIslandSubtype() {
        harness.addToBattlefield(player1, new StormtideLeviathan());

        Card creature = new Card();
        creature.setName("Test Creature");
        creature.setType(CardType.CREATURE);
        creature.setSubtypes(List.of());
        Permanent creaturePerm = new Permanent(creature);
        gd.playerBattlefields.get(player1.getId()).add(creaturePerm);

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, creaturePerm);
        assertThat(bonus.grantedSubtypes()).doesNotContain(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("Creature with flying can attack when Stormtide Leviathan is on the battlefield")
    void flyingCreatureCanAttack() {
        harness.addToBattlefield(player1, new StormtideLeviathan());
        harness.setLife(player2, 20);

        Card flyer = new Card();
        flyer.setName("Test Flyer");
        flyer.setType(CardType.CREATURE);
        flyer.setSubtypes(new ArrayList<>());
        flyer.setKeywords(Set.of(Keyword.FLYING));
        flyer.setPower(2);
        flyer.setToughness(2);
        Permanent flyerPerm = addCreatureReady(player1, flyer);

        int flyerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(flyerPerm);
        declareAttackers(player1, List.of(flyerIndex));

        // Combat auto-advances; verify attack went through by checking damage dealt
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Creature with islandwalk can attack when Stormtide Leviathan is on the battlefield")
    void islandwalkCreatureCanAttack() {
        harness.addToBattlefield(player1, new StormtideLeviathan());
        harness.setLife(player2, 20);

        Card walker = new Card();
        walker.setName("Test Islandwalker");
        walker.setType(CardType.CREATURE);
        walker.setSubtypes(new ArrayList<>());
        walker.setKeywords(Set.of(Keyword.ISLANDWALK));
        walker.setPower(2);
        walker.setToughness(2);
        Permanent walkerPerm = addCreatureReady(player1, walker);

        int walkerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(walkerPerm);
        declareAttackers(player1, List.of(walkerIndex));

        // Combat auto-advances; verify attack went through by checking damage dealt
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Creature without flying or islandwalk cannot attack when Stormtide Leviathan is on the battlefield")
    void groundCreatureCannotAttack() {
        harness.addToBattlefield(player1, new StormtideLeviathan());

        Card grunt = new Card();
        grunt.setName("Test Grunt");
        grunt.setType(CardType.CREATURE);
        grunt.setSubtypes(new ArrayList<>());
        grunt.setPower(3);
        grunt.setToughness(3);
        Permanent gruntPerm = addCreatureReady(player1, grunt);

        int gruntIndex = gd.playerBattlefields.get(player1.getId()).indexOf(gruntPerm);
        assertThatThrownBy(() -> declareAttackers(player1, List.of(gruntIndex)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponent's ground creature also cannot attack when Stormtide Leviathan is on the battlefield")
    void opponentGroundCreatureCannotAttack() {
        harness.addToBattlefield(player1, new StormtideLeviathan());

        Card grunt = new Card();
        grunt.setName("Opponent Grunt");
        grunt.setType(CardType.CREATURE);
        grunt.setSubtypes(new ArrayList<>());
        grunt.setPower(3);
        grunt.setToughness(3);
        addCreatureReady(player2, grunt);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Stormtide Leviathan itself can attack (has islandwalk)")
    void stormtideLeviathanCanAttack() {
        addCreatureReady(player1, new StormtideLeviathan());

        harness.setLife(player2, 20);
        declareAttackers(player1, List.of(0));

        // Combat auto-advances; Stormtide Leviathan is 8/8
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Ground creatures can attack again after Stormtide Leviathan leaves the battlefield")
    void restrictionLiftsWhenLeviathanLeaves() {
        Permanent leviathanPerm = addCreatureReady(player1, new StormtideLeviathan());

        Card grunt = new Card();
        grunt.setName("Test Grunt");
        grunt.setType(CardType.CREATURE);
        grunt.setSubtypes(new ArrayList<>());
        grunt.setPower(3);
        grunt.setToughness(3);
        addCreatureReady(player1, grunt);

        // Remove Stormtide Leviathan from battlefield
        gd.playerBattlefields.get(player1.getId()).remove(leviathanPerm);

        harness.setLife(player2, 20);
        declareAttackers(player1, List.of(0));

        // Combat auto-advances; grunt is 3/3
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void landsRetainOriginalTypesAndGainBlueMana() {
        harness.addToBattlefield(player1, new StormtideLeviathan());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        assertThat(gqs.effectiveBasicLandTypes(gd, forest))
                .containsExactlyInAnyOrder(CardSubtype.FOREST, CardSubtype.ISLAND);
        assertThat(gqs.effectiveBasicLandTypes(gd, mountain))
                .containsExactlyInAnyOrder(CardSubtype.MOUNTAIN, CardSubtype.ISLAND);
        assertThat(gqs.intrinsicBasicLandManaColors(gd, forest))
                .containsExactlyInAnyOrder(ManaColor.GREEN, ManaColor.BLUE);
        assertThat(gqs.intrinsicBasicLandManaColors(gd, mountain))
                .containsExactlyInAnyOrder(ManaColor.RED, ManaColor.BLUE);
    }

    @Test
    void islandGrantEndsWhenLeviathanLeaves() {
        Permanent leviathan = harness.addToBattlefieldAndReturn(player1, new StormtideLeviathan());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        assertThat(gqs.hasEffectiveSubtype(gd, forest, CardSubtype.ISLAND)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(leviathan);

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.FOREST);
        assertThat(gqs.intrinsicBasicLandManaColors(gd, forest)).containsExactly(ManaColor.GREEN);
    }

    @Test
    void islandwalkPreventsBlockingWithAnOriginallyNonIslandLand() {
        addCreatureReady(player1, new StormtideLeviathan());
        harness.addToBattlefield(player2, new Mountain());
        addCreatureReady(player2, new MaritimeGuard());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void islandwalkDoesNotPreventBlockingWhenDefenderHasNoLands() {
        addCreatureReady(player1, new StormtideLeviathan());
        harness.addToBattlefield(player1, new Forest());
        Permanent blocker = addCreatureReady(player2, new MaritimeGuard());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
