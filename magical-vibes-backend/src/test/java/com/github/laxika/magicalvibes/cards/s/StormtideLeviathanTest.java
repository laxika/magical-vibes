package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StormtideLeviathanTest extends BaseCardTest {

    // ===== Card effects =====

    @Test
    @DisplayName("Stormtide Leviathan has GrantSubtypeEffect and CreaturesCantAttackUnlessPredicateEffect")
    void hasCorrectEffects() {
        StormtideLeviathan card = new StormtideLeviathan();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.STATIC).get(0)).isInstanceOf(GrantSubtypeEffect.class);
        assertThat(card.getEffects(EffectSlot.STATIC).get(1)).isInstanceOf(CreaturesCantAttackUnlessPredicateEffect.class);
    }

    // ===== All lands are Islands =====

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

    // ===== Attack restrictions =====

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
        Permanent flyerPerm = new Permanent(flyer);
        flyerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(flyerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        int flyerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(flyerPerm);
        gs.declareAttackers(gd, player1, List.of(flyerIndex));

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
        Permanent walkerPerm = new Permanent(walker);
        walkerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(walkerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        int walkerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(walkerPerm);
        gs.declareAttackers(gd, player1, List.of(walkerIndex));

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
        Permanent gruntPerm = new Permanent(grunt);
        gruntPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(gruntPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        int gruntIndex = gd.playerBattlefields.get(player1.getId()).indexOf(gruntPerm);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(gruntIndex)))
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
        Permanent gruntPerm = new Permanent(grunt);
        gruntPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(gruntPerm);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Stormtide Leviathan itself can attack (has islandwalk)")
    void stormtideLeviathanCanAttack() {
        Permanent leviathanPerm = new Permanent(new StormtideLeviathan());
        leviathanPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(leviathanPerm);

        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));

        // Combat auto-advances; Stormtide Leviathan is 8/8
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Ground creatures can attack again after Stormtide Leviathan leaves the battlefield")
    void restrictionLiftsWhenLeviathanLeaves() {
        Permanent leviathanPerm = new Permanent(new StormtideLeviathan());
        leviathanPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(leviathanPerm);

        Card grunt = new Card();
        grunt.setName("Test Grunt");
        grunt.setType(CardType.CREATURE);
        grunt.setSubtypes(new ArrayList<>());
        grunt.setPower(3);
        grunt.setToughness(3);
        Permanent gruntPerm = new Permanent(grunt);
        gruntPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(gruntPerm);

        // Remove Stormtide Leviathan from battlefield
        gd.playerBattlefields.get(player1.getId()).remove(leviathanPerm);

        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));

        // Combat auto-advances; grunt is 3/3
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
