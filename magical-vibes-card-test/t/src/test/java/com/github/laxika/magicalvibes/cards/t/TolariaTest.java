package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AdventurersGuildhouse;
import com.github.laxika.magicalvibes.cards.f.FortifiedArea;
import com.github.laxika.magicalvibes.cards.j.JasmineBoreal;
import com.github.laxika.magicalvibes.cards.m.MasterOfTheHunt;
import com.github.laxika.magicalvibes.cards.w.WallOfCaltrops;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AdventurersGuildhouse.class, FortifiedArea.class, JasmineBoreal.class,
        MasterOfTheHunt.class, Tolaria.class, WallOfCaltrops.class})
class TolariaTest extends BaseCardTest {

    @Test
    void addsBlueMana() {
        Permanent tolaria = harness.addToBattlefieldAndReturn(player1, new Tolaria());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(tolaria.isTapped()).isTrue();
    }

    @Test
    void removesBandingUntilEndOfTurnDuringAnyUpkeep() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfCaltrops());
        harness.addToBattlefield(player1, new FortifiedArea());
        Permanent tolaria = harness.addToBattlefieldAndReturn(player1, new Tolaria());
        assertThat(gqs.hasKeyword(gd, wall, Keyword.BANDING)).isTrue();

        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        int tolariaIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tolaria);
        harness.activateAbility(player1, tolariaIndex, 1, null, wall.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wall, Keyword.BANDING)).isFalse();
        assertThat(tolaria.isTapped()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wall, Keyword.BANDING)).isTrue();
    }

    @Test
    void removesBandsWithOtherUntilEndOfTurn() {
        harness.addToBattlefield(player1, new MasterOfTheHunt());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent wolf = findPermanent(player1, "Wolves of the Hunt");
        Permanent tolaria = harness.addToBattlefieldAndReturn(player1, new Tolaria());
        assertThat(gqs.bandsWithOtherNames(gd, wolf)).containsExactly("Wolves of the Hunt");

        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        int tolariaIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tolaria);
        harness.activateAbility(player1, tolariaIndex, 1, null, wolf.getId());
        harness.passBothPriorities();

        assertThat(gqs.bandsWithOtherNames(gd, wolf)).isEmpty();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.bandsWithOtherNames(gd, wolf)).containsExactly("Wolves of the Hunt");
    }

    @Test
    void cannotActivateCreatureAbilityOutsideUpkeep() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfCaltrops());
        Permanent tolaria = harness.addToBattlefieldAndReturn(player1, new Tolaria());

        int tolariaIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tolaria);
        assertThatThrownBy(() -> harness.activateAbility(player1, tolariaIndex, 1, null, wall.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canActivateCreatureAbilityDuringOpponentsUpkeep() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfCaltrops());
        harness.addToBattlefield(player1, new FortifiedArea());
        Permanent tolaria = harness.addToBattlefieldAndReturn(player1, new Tolaria());
        assertThat(gqs.hasKeyword(gd, wall, Keyword.BANDING)).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        int tolariaIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tolaria);
        harness.activateAbility(player1, tolariaIndex, 1, null, wall.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wall, Keyword.BANDING)).isFalse();
    }

    @Test
    void cannotTargetNonCreature() {
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player1, new FortifiedArea());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Tolaria());

        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        assertThatThrownBy(() -> harness.activateAbility(player1, sourceIndex, 1, null, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.isTapped()).isFalse();
    }

    @Test
    void laterBandsWithOtherGrantRestoresAbility() {
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new JasmineBoreal());
        Permanent tolaria = harness.enterBattlefieldAndReturn(player1, new Tolaria());
        harness.forceStep(TurnStep.UPKEEP);
        int tolariaIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tolaria);

        harness.activateAbility(player1, tolariaIndex, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.canUseBandsWithOther(gd, List.of(creature), false)).isFalse();

        harness.enterBattlefieldAndReturn(player1, new AdventurersGuildhouse());

        assertThat(gqs.canUseBandsWithOther(gd, List.of(creature), false)).isTrue();
    }

    @Test
    void removesPreviouslyGrantedBandsWithOther() {
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new JasmineBoreal());
        harness.enterBattlefieldAndReturn(player1, new AdventurersGuildhouse());
        Permanent tolaria = harness.enterBattlefieldAndReturn(player1, new Tolaria());
        assertThat(gqs.canUseBandsWithOther(gd, List.of(creature), false)).isTrue();
        harness.forceStep(TurnStep.UPKEEP);
        int tolariaIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tolaria);

        harness.activateAbility(player1, tolariaIndex, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.canUseBandsWithOther(gd, List.of(creature), false)).isFalse();
    }

    @Test
    void targetsOpponentsCreatureAndPreservesOtherAbilitiesAndCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WallOfCaltrops());
        Permanent otherWall = harness.addToBattlefieldAndReturn(player2, new WallOfCaltrops());
        harness.addToBattlefield(player2, new FortifiedArea());
        harness.addToBattlefield(player1, new Tolaria());
        harness.forceStep(TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, target, Keyword.BANDING)).isTrue();

        harness.activateAbility(player1, 0, 1, null, target.getId());

        assertThat(gqs.hasKeyword(gd, target, Keyword.BANDING)).isTrue();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.BANDING)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherWall, Keyword.BANDING)).isTrue();
    }

    @Test
    void laterBandingGrantRestoresBanding() {
        Permanent wall = harness.enterBattlefieldAndReturn(player1, new WallOfCaltrops());
        Permanent tolaria = harness.enterBattlefieldAndReturn(player1, new Tolaria());
        harness.forceStep(TurnStep.UPKEEP);
        int tolariaIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tolaria);

        harness.activateAbility(player1, tolariaIndex, 1, null, wall.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wall, Keyword.BANDING)).isFalse();

        harness.enterBattlefieldAndReturn(player1, new FortifiedArea());

        assertThat(gqs.hasKeyword(gd, wall, Keyword.BANDING)).isTrue();
    }
}
