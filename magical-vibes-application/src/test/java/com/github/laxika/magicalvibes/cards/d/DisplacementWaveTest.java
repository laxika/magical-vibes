package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AlchemistsVial;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GaeasRevenge;
import com.github.laxika.magicalvibes.cards.h.HangarbackWalker;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DisplacementWave.class, GrizzlyBears.class, HillGiant.class, Island.class,
        LlanowarElves.class, Ornithopter.class, SerraAngel.class, AlchemistsVial.class,
        GaeasRevenge.class, HangarbackWalker.class})
class DisplacementWaveTest extends BaseCardTest {

    private void castForX(int xValue) {
        harness.setHand(player1, List.of(new DisplacementWave()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        if (xValue > 0) {
            harness.addMana(player1, ManaColor.COLORLESS, xValue);
        }
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castAndResolveSorcery(player1, 0, xValue);
    }

    @Test
    @DisplayName("Bounces nonland permanents with mana value X or less on both sides")
    void bouncesPermanentsWithinX() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new SerraAngel());

        castForX(2);

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player2, "Serra Angel");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactlyInAnyOrder("Ornithopter", "Llanowar Elves");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .contains("Grizzly Bears");
        harness.assertInGraveyard(player1, "Displacement Wave");
    }

    @Test
    @DisplayName("X=0 only bounces zero-mana-value permanents")
    void xZeroBouncesOnlyFreePermanents() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new LlanowarElves());

        castForX(0);

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Ornithopter");
    }

    @Test
    @DisplayName("Lands are never returned regardless of X")
    void landsStay() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new SerraAngel());

        castForX(5);

        harness.assertOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player2, "Island");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(p -> p.getCard().getName())
                .containsExactly("Island");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .contains("Serra Angel");
    }

    @Test
    @DisplayName("Returns noncreature artifacts at the mana value boundary on both sides")
    void returnsNoncreatureArtifacts() {
        harness.addToBattlefield(player1, new AlchemistsVial());
        harness.addToBattlefield(player2, new AlchemistsVial());

        castForX(2);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInHand(player1, "Alchemist's Vial");
        harness.assertInHand(player2, "Alchemist's Vial");
    }

    @Test
    @DisplayName("Returns a permanent to its owner rather than its current controller")
    void returnsToOwnerDespiteChangedControl() {
        GrizzlyBears bears = new GrizzlyBears();
        bears.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, bears);

        castForX(2);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Resolves without targets when no permanent qualifies")
    void resolvesWithNoMatchingPermanents() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castForX(1);

        harness.assertOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Displacement Wave");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ignores protection from being targeted by nongreen spells")
    void returnsPermanentWithTargetingRestriction() {
        harness.addToBattlefield(player2, new GaeasRevenge());

        castForX(7);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInHand(player2, "Gaea's Revenge");
    }

    @Test
    @DisplayName("X in a permanent's mana cost counts as zero regardless of its counters")
    void xZeroReturnsHangarbackWalkerWithCounters() {
        harness.addToBattlefieldAndReturn(player2, new HangarbackWalker())
                .setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        castForX(0);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInHand(player2, "Hangarback Walker");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
