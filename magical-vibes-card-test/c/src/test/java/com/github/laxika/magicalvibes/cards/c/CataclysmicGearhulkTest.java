package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.IronStar;
import com.github.laxika.magicalvibes.cards.l.LilianaOfTheVeil;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CataclysmicGearhulk.class, GrizzlyBears.class, HillGiant.class, IronStar.class,
        LilianaOfTheVeil.class, Millstone.class, Plains.class, Crusade.class, DarksteelCitadel.class})
class CataclysmicGearhulkTest extends BaseCardTest {

    @Test
    @DisplayName("Each player keeps one nonland permanent of each type and lands survive")
    void eachPlayerChoosesNonlandsAndLandsSurvive() {
        Permanent ownMillstone = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownCrusade = harness.addToBattlefieldAndReturn(player1, new Crusade());
        Permanent ownLiliana = harness.addToBattlefieldAndReturn(player1, new LilianaOfTheVeil());
        ownLiliana.setCounterCount(CounterType.LOYALTY, 3);
        Permanent ownPlains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent ownIronStar = harness.addToBattlefieldAndReturn(player1, new IronStar());
        Permanent ownHillGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        Permanent opponentMillstone = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opponentCrusade = harness.addToBattlefieldAndReturn(player2, new Crusade());
        Permanent opponentLiliana = harness.addToBattlefieldAndReturn(player2, new LilianaOfTheVeil());
        opponentLiliana.setCounterCount(CounterType.LOYALTY, 3);
        Permanent opponentPlains = harness.addToBattlefieldAndReturn(player2, new Plains());
        Permanent opponentIronStar = harness.addToBattlefieldAndReturn(player2, new IronStar());
        Permanent opponentHillGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new CataclysmicGearhulk()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.handleMultiplePermanentsChosen(player1, List.of(ownMillstone.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(ownBears.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(opponentMillstone.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(opponentBears.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(ownMillstone, ownBears, ownCrusade, ownLiliana, ownPlains)
                .doesNotContain(ownIronStar, ownHillGiant);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(opponentMillstone, opponentBears, opponentCrusade, opponentLiliana, opponentPlains)
                .doesNotContain(opponentIronStar, opponentHillGiant);
        harness.assertInGraveyard(player1, "Iron Star");
        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Iron Star");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Cataclysmic Gearhulk");
    }

    @Test
    @DisplayName("The same artifact creature can be kept for both types")
    void canChooseSamePermanentForArtifactAndCreature() {
        harness.forceActivePlayer(player1);
        Permanent previousGearhulk = harness.addToBattlefieldAndReturn(player1, new CataclysmicGearhulk());
        harness.setHand(player1, List.of(new CataclysmicGearhulk()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.handleMultiplePermanentsChosen(player1, List.of(previousGearhulk.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(previousGearhulk.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(previousGearhulk);
        harness.assertInGraveyard(player1, "Cataclysmic Gearhulk");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An artifact land cannot be chosen as the artifact to keep")
    void artifactLandCannotBeChosen() {
        harness.forceActivePlayer(player1);
        Permanent citadel = harness.addToBattlefieldAndReturn(player1, new DarksteelCitadel());
        Permanent millstone = harness.addToBattlefieldAndReturn(player1, new Millstone());
        harness.setHand(player1, List.of(new CataclysmicGearhulk()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(citadel.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(millstone.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(citadel, millstone);
        harness.assertOnBattlefield(player1, "Cataclysmic Gearhulk");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A player cannot skip choosing a type they control")
    void cannotDeclineMandatoryChoice() {
        harness.forceActivePlayer(player1);
        Permanent previousGearhulk = harness.addToBattlefieldAndReturn(player1, new CataclysmicGearhulk());
        harness.setHand(player1, List.of(new CataclysmicGearhulk()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(previousGearhulk.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(previousGearhulk.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(previousGearhulk);
        harness.assertInGraveyard(player1, "Cataclysmic Gearhulk");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
