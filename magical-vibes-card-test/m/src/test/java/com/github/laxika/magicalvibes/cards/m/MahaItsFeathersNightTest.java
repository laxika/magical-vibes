package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.f.Fell;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MahaItsFeathersNight.class, GloriousAnthem.class, HillGiant.class, Fell.class})
class MahaItsFeathersNightTest extends BaseCardTest {

    @Test
    void setsOpponentsCreaturesBaseToughnessToOne() {
        Permanent maha = addCreatureReady(player1, new MahaItsFeathersNight());
        Permanent ownCreature = addCreatureReady(player1, new HillGiant());
        Permanent opponentCreature = addCreatureReady(player2, new HillGiant());
        harness.addToBattlefield(player2, new GloriousAnthem());

        assertThat(gqs.getEffectivePower(gd, maha)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, maha)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    void countersApplyAfterBaseToughnessIsSet() {
        harness.addToBattlefield(player1, new MahaItsFeathersNight());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new MahaItsFeathersNight());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(3);
    }

    @Test
    void toughnessReturnsWhenMahaLeavesTheBattlefield() {
        Permanent maha = harness.addToBattlefieldAndReturn(player1, new MahaItsFeathersNight());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new MahaItsFeathersNight());
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(1);

        harness.setHand(player1, List.of(new Fell()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, maha.getId());

        harness.assertInGraveyard(player1, "Maha, Its Feathers Night");
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(5);
    }

    @Test
    void wardCountersOpponentsRemovalWhenTheyCannotDiscard() {
        Permanent maha = harness.addToBattlefieldAndReturn(player1, new MahaItsFeathersNight());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Fell()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castSorcery(player2, 0, maha.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Maha, Its Feathers Night");
        harness.assertInGraveyard(player2, "Fell");
    }
}
