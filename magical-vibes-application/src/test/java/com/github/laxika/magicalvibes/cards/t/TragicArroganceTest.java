package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Crusade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.IronStar;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.cards.l.LilianaOfTheVeil;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TragicArroganceTest extends BaseCardTest {

    private void cast() {
        harness.setHand(player1, List.of(new TragicArrogance()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The caster keeps one artifact and one creature per player; the rest are sacrificed")
    void keepsOneOfEachTypePerPlayer() {
        Permanent millstone = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent ironStar = harness.addToBattlefieldAndReturn(player1, new IronStar());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent theirBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast();

        harness.handleMultiplePermanentsChosen(player1, List.of(millstone.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(elves.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(millstone, bears, plains);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ironStar, hillGiant);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(elves);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(theirBears);
        harness.assertInGraveyard(player1, "Iron Star");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A type with a single candidate is kept without a choice")
    void singleCandidateIsKeptAutomatically() {
        Permanent millstone = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        cast();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(millstone, bears);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(elves);
    }

    @Test
    @DisplayName("An artifact creature can be kept as both the artifact and the creature")
    void artifactCreatureCanBeKeptForBothTypes() {
        Permanent juggernaut = harness.addToBattlefieldAndReturn(player2, new Juggernaut());
        Permanent millstone = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast();

        harness.handleMultiplePermanentsChosen(player1, List.of(juggernaut.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(juggernaut.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(juggernaut);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(millstone, bears);
        harness.assertInGraveyard(player2, "Millstone");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Enchantments and planeswalkers are kept alongside the artifact and creature")
    void enchantmentAndPlaneswalkerSurvive() {
        Permanent crusade = harness.addToBattlefieldAndReturn(player1, new Crusade());
        Permanent liliana = harness.addToBattlefieldAndReturn(player1, new LilianaOfTheVeil());
        liliana.setCounterCount(CounterType.LOYALTY, 3);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        cast();

        harness.handleMultiplePermanentsChosen(player1, List.of(hillGiant.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(crusade, liliana, hillGiant);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
