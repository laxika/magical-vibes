package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({RahildaWantedCutthroat.class, RahildaFeralOutlaw.class, Forest.class, GrizzlyBears.class})
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

    private void resolveCombatAndTrigger() {
        declareAttackers(List.of(0));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
    }

}
