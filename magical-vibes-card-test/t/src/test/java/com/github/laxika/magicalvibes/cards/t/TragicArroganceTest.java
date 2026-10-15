package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Crusade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HithlainRope;
import com.github.laxika.magicalvibes.cards.i.IronStar;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.cards.l.LilianaOfTheVeil;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TragicArrogance.class, Crusade.class, GrizzlyBears.class, HillGiant.class,
        HithlainRope.class, IronStar.class, Juggernaut.class, LilianaOfTheVeil.class, LlanowarElves.class,
        Millstone.class, Plains.class})
class TragicArroganceTest extends BaseCardTest {

    private void cast() {
        harness.castFromHand(player1, new TragicArrogance(), "{3}{W}{W}");
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
    @Test
    @DisplayName("An artifact creature and a different creature can both be kept")
    void artifactCreatureCanBeKeptAlongsideAnotherCreature() {
        Permanent juggernaut = harness.addToBattlefieldAndReturn(player2, new Juggernaut());
        Permanent millstone = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast();

        harness.handleMultiplePermanentsChosen(player1, List.of(juggernaut.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(juggernaut, millstone, bears);
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactlyInAnyOrder(juggernaut, bears);
        harness.assertInGraveyard(player2, "Millstone");
    }

    @Test
    @DisplayName("The caster chooses which enchantment the opponent keeps")
    void choosesOpponentsEnchantment() {
        Permanent kept = harness.addToBattlefieldAndReturn(player2, new Crusade());
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player2, new Crusade());

        cast();
        harness.handleMultiplePermanentsChosen(player1, List.of(kept.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(sacrificed.getCard());
    }

    @Test
    @DisplayName("Tragic Arrogance resolves with no permanents to choose")
    void resolvesOnEmptyBattlefields() {
        cast();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Tragic Arrogance");
    }
    @Test
    @CardUsed({TragicArrogance.class, HithlainRope.class, Millstone.class})
    @DisplayName("An unchosen artifact that cannot be sacrificed remains on the battlefield")
    void unchosenArtifactCannotBeSacrificed() {
        Permanent rope = harness.addToBattlefieldAndReturn(player2, new HithlainRope());
        Permanent millstone = harness.addToBattlefieldAndReturn(player2, new Millstone());

        cast();
        harness.handleMultiplePermanentsChosen(player1, List.of(millstone.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactlyInAnyOrder(rope, millstone);
        harness.assertNotInGraveyard(player2, "Hithlain Rope");
    }

    @Test
    @DisplayName("Choosing a creature is mandatory when candidates exist")
    void cannotDeclineMandatoryCreatureChoice() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        cast();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactlyInAnyOrder(bears, giant);

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(bears);
        harness.assertInGraveyard(player2, "Hill Giant");
    }
}
