package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.ForestBear;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.PaleBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Winnowing.class, GrizzlyBears.class, PaleBears.class, HillGiant.class,
        WoodlandChangeling.class, ForestBear.class})
class WinnowingTest extends BaseCardTest {

    private void cast() {
        harness.setHand(player1, List.of(new Winnowing()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    @Test
    @DisplayName("The caster chooses one creature per player and sacrifices non-sharing creatures")
    void choosesCreatureForEachPlayer() {
        Permanent chosenBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent otherBear = harness.addToBattlefieldAndReturn(player1, new PaleBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new ForestBear());
        Permanent opponentGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        cast();

        harness.handleMultiplePermanentsChosen(player1, List.of(chosenBear.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(opponentBear.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsExactly(chosenBear, otherBear)
                .doesNotContain(giant);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .containsExactly(opponentBear)
                .doesNotContain(opponentGiant);
        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Changeling shares a creature type with the chosen creature")
    void changelingSharesCreatureType() {
        Permanent chosenBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        cast();

        harness.handleMultiplePermanentsChosen(player1, List.of(chosenBear.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsExactly(chosenBear, changeling)
                .doesNotContain(giant);
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Choosing a changeling preserves creatures of different types")
    void chosenChangelingPreservesAllTypedCreatures() {
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        cast();
        harness.handleMultiplePermanentsChosen(player1, List.of(changeling.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(changeling, bear, giant);
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card instanceof GrizzlyBears
                || card instanceof HillGiant || card instanceof WoodlandChangeling);
    }

    @Test
    @DisplayName("A lone creature for each player is preserved without requiring input")
    void singleCreatureForEachPlayer() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        cast();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(bear);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(giant);
        harness.assertInGraveyard(player1, "Winnowing");
    }

    @Test
    @DisplayName("An empty battlefield does not require creature choices")
    void noCreatures() {
        cast();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Winnowing");
    }

    @Test
    @DisplayName("A summoning-sick green creature can convoke one generic mana")
    void convokePaysGenericMana() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Winnowing()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(bear.getId()));
        assertThat(bear.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(bear);
        harness.assertInGraveyard(player1, "Winnowing");
    }
}
