package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KozilekTheGreatDistortion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RahildaWantedCutthroat.class, RahildaFeralOutlaw.class, Forest.class, GrizzlyBears.class,
        KozilekTheGreatDistortion.class})
class RahildaWantedCutthroatTest extends BaseCardTest {

    @Test
    void combatDamageExilesRandomNonlandFromEntireLibraryAndTracksIt() {
        Permanent rahilda = addCreatureReady(player1, new RahildaWantedCutthroat());
        Card land = new Forest();
        Card nonland = new GrizzlyBears();
        harness.setLibrary(player2, List.of(land, nonland));

        resolveCombatAndTrigger();

        ExiledCardEntry exiled = gd.findExiledCard(nonland.getId());
        assertThat(exiled).isNotNull();
        assertThat(exiled.faceDown()).isFalse();
        assertThat(exiled.sourcePermanentId()).isEqualTo(rahilda.getId());
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(land);
    }

    @Test
    void attackedWolfOrWerewolfAllowsCastingTheExiledCardWithAnyColor() {
        addCreatureReady(player1, new RahildaWantedCutthroat());
        Card land = new Forest();
        Card nonland = new GrizzlyBears();
        harness.setLibrary(player2, List.of(land, nonland));

        resolveCombatAndTrigger();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castFromExile(player1, nonland.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(nonland.getId())).isNull();
    }

    @Test
    void dayAndNightTransformRahilda() {
        gd.dayNight = DayNight.DAY;
        Permanent rahilda = harness.enterBattlefieldAndReturn(player1, new RahildaWantedCutthroat());

        gd.spellsCastLastTurn.clear();
        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(rahilda.getCard()).isInstanceOf(RahildaFeralOutlaw.class);

        gd.spellsCastLastTurn.put(player1.getId(), 2);
        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(rahilda.getCard()).isInstanceOf(RahildaWantedCutthroat.class);
    }

    @Test
    void exiledCardCanBeCastOnALaterTurnAfterAttackingAgain() {
        Permanent rahilda = addCreatureReady(player1, new RahildaWantedCutthroat());
        Card nonland = new GrizzlyBears();
        harness.setLibrary(player2, List.of(nonland));
        resolveCombatAndTrigger();

        gd.turnNumber++;
        gd.creaturesAttackedCountBySubtypeThisTurn.clear();
        rahilda.setTapped(false);
        resolveCombatAndTrigger();

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castFromExile(player1, nonland.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void castingPermissionSurvivesRahildaLeavingTheBattlefield() {
        Permanent rahilda = addCreatureReady(player1, new RahildaWantedCutthroat());
        Card nonland = new GrizzlyBears();
        harness.setLibrary(player2, List.of(nonland));
        resolveCombatAndTrigger();

        gd.playerBattlefields.get(player1.getId()).remove(rahilda);
        harness.setGraveyard(player1, List.of(rahilda.getOriginalCard()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castFromExile(player1, nonland.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void cannotCastAnOldExiledCardBeforeAttackingThisTurn() {
        addCreatureReady(player1, new RahildaWantedCutthroat());
        Card nonland = new GrizzlyBears();
        harness.setLibrary(player2, List.of(nonland));
        resolveCombatAndTrigger();

        gd.turnNumber++;
        gd.creaturesAttackedCountBySubtypeThisTurn.clear();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, nonland.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(nonland.getId())).isNotNull();
    }

    @Test
    void allLandLibraryIsUnaffected() {
        addCreatureReady(player1, new RahildaWantedCutthroat());
        Card land = new Forest();
        harness.setLibrary(player2, List.of(land));

        resolveCombatAndTrigger();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(land);
        assertThat(gd.findExiledCard(land.getId())).isNull();
    }

    @Test
    void emptyLibraryDoesNotPreventCombatDamage() {
        addCreatureReady(player1, new RahildaWantedCutthroat());
        harness.setLibrary(player2, List.of());
        harness.setLife(player2, 20);

        resolveCombatAndTrigger();

        harness.assertLife(player2, 18);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void anyColorPermissionDoesNotPayColorlessManaRequirements() {
        addCreatureReady(player1, new RahildaWantedCutthroat());
        Card nonland = new KozilekTheGreatDistortion();
        harness.setLibrary(player2, List.of(nonland));
        resolveCombatAndTrigger();
        harness.addMana(player1, ManaColor.WHITE, 10);

        assertThatThrownBy(() -> harness.castFromExile(player1, nonland.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(nonland.getId())).isNotNull();
    }

    @Test
    void nightFaceDealsDamageAndExilesACardInBothCombatDamageSteps() {
        gd.dayNight = DayNight.NIGHT;
        Permanent rahilda = harness.enterBattlefieldAndReturn(player1, new RahildaWantedCutthroat());
        rahilda.setSummoningSick(false);
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player2, List.of(first, second));
        harness.setLife(player2, 20);

        resolveCombatAndTrigger();

        harness.assertLife(player2, 16);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.findExiledCard(first.getId())).isNotNull();
        assertThat(gd.findExiledCard(second.getId())).isNotNull();
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castFromExile(player1, first.getId());
        harness.passBothPriorities();
        harness.castFromExile(player1, second.getId());
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(2);
    }

    private void resolveCombatAndTrigger() {
        declareAttackers(List.of(0));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
    }

}
