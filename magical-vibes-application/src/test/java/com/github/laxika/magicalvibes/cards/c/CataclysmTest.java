package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.i.IronStar;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CataclysmTest extends BaseCardTest {

    private void cast() {
        harness.setHand(player1, List.of(new Cataclysm()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("A player keeps one artifact, creature, enchantment, and land; the rest are sacrificed")
    void keepsOneOfEachTypeAndSacrificesTheRest() {
        Permanent millstone = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent ironStar = harness.addToBattlefieldAndReturn(player1, new IronStar());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent crusade = harness.addToBattlefieldAndReturn(player1, new Crusade());
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent secondPlains = harness.addToBattlefieldAndReturn(player1, new Plains());

        cast();

        harness.handleMultiplePermanentsChosen(player1, List.of(millstone.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(plains.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(millstone, bears, crusade, plains);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(ironStar, hillGiant, secondPlains);
        harness.assertInGraveyard(player1, "Iron Star");
        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Plains");
    }

    @Test
    @DisplayName("An artifact creature can be kept for both the artifact and creature choices")
    void artifactCreatureCanBeKeptForBothTypes() {
        Permanent juggernaut = harness.addToBattlefieldAndReturn(player1, new Juggernaut());
        Permanent millstone = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent crusade = harness.addToBattlefieldAndReturn(player1, new Crusade());
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent secondPlains = harness.addToBattlefieldAndReturn(player1, new Plains());

        cast();

        harness.handleMultiplePermanentsChosen(player1, List.of(juggernaut.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(juggernaut.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(plains.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(juggernaut, crusade, plains);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(millstone, bears, secondPlains);
        harness.assertInGraveyard(player1, "Millstone");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Plains");
    }

    @Test
    @DisplayName("Each player chooses the permanents they control")
    void eachPlayerMakesTheirOwnChoices() {
        Permanent ownMillstone = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownCrusade = harness.addToBattlefieldAndReturn(player1, new Crusade());
        Permanent ownPlains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent opponentMillstone = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Permanent opponentIronStar = harness.addToBattlefieldAndReturn(player2, new IronStar());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opponentHillGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent opponentCrusade = harness.addToBattlefieldAndReturn(player2, new Crusade());
        Permanent opponentPlains = harness.addToBattlefieldAndReturn(player2, new Plains());
        Permanent opponentSecondPlains = harness.addToBattlefieldAndReturn(player2, new Plains());

        harness.forceActivePlayer(player1);
        cast();

        harness.handleMultiplePermanentsChosen(player2, List.of(opponentMillstone.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(opponentBears.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(opponentPlains.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownMillstone, ownBears, ownCrusade, ownPlains);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(opponentMillstone, opponentBears, opponentCrusade, opponentPlains)
                .doesNotContain(opponentIronStar, opponentHillGiant, opponentSecondPlains);
    }
}
