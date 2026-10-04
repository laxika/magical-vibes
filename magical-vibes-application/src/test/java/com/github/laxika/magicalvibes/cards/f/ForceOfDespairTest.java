package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForceOfDespair.class, GrizzlyBears.class, DoomBlade.class, HowlingMine.class, MycosynthLattice.class})
class ForceOfDespairTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys only creatures that entered the battlefield this turn")
    void destroysOnlyCreaturesThatEnteredThisTurn() {
        Permanent olderCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent enteredCreature = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new HowlingMine());

        castNormally();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(olderCreature);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Howling Mine");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Force of Despair");
        assertThat(enteredCreature).isNotIn(gd.playerBattlefields.get(player2.getId()));
    }

    @Test
    @DisplayName("Can be cast by exiling a black card from hand on an opponent's turn")
    void castsForAlternateCostOnOpponentsTurn() {
        Permanent enteredCreature = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        DoomBlade blackCard = new DoomBlade();
        harness.setHand(player1, List.of(new ForceOfDespair(), blackCard));
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();

        harness.castInstantWithAlternateExileFromHand(player1, 0, (java.util.UUID) null, 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Force of Despair");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName()).containsExactly("Doom Blade");
        assertThat(enteredCreature).isNotIn(gd.playerBattlefields.get(player2.getId()));
    }

    @Test
    @DisplayName("Cannot use its alternate cost during its controller's turn")
    void alternateCostUnavailableOnOwnTurn() {
        DoomBlade blackCard = new DoomBlade();
        harness.setHand(player1, List.of(new ForceOfDespair(), blackCard));

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(player1, 0, (java.util.UUID) null, 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroys newly entered creatures controlled by either player but spares new noncreatures")
    void destroysNewCreaturesOnBothSides() {
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new HowlingMine());

        castNormally();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Howling Mine");
    }

    @Test
    @DisplayName("Resolves without any creatures that entered this turn")
    void resolvesWithoutNewCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castNormally();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Force of Despair");
    }

    @Test
    @DisplayName("Checks which creatures entered this turn when the spell resolves")
    void includesCreaturesEnteringAfterCasting() {
        harness.castFromHand(player1, new ForceOfDespair(), "{1}{B}{B}");
        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Force of Despair");
    }

    @Test
    @DisplayName("Cannot exile a nonblack card for its alternate cost")
    void rejectsNonblackExilePayment() {
        harness.setHand(player1, List.of(new ForceOfDespair(), new GrizzlyBears()));
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(player1, 0, (java.util.UUID) null, 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot exile a printed black card made colorless by Mycosynth Lattice")
    void rejectsPaymentMadeColorless() {
        harness.addToBattlefield(player2, new MycosynthLattice());
        harness.setHand(player1, List.of(new ForceOfDespair(), new DoomBlade()));
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(player1, 0, (java.util.UUID) null, 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot exile the spell itself to pay its alternate cost")
    void rejectsExilingItself() {
        harness.setHand(player1, List.of(new ForceOfDespair()));
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(player1, 0, (java.util.UUID) null, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can exile another Force of Despair with a hand index before the spell")
    void exilesAnotherCopyBeforeSpellIndex() {
        ForceOfDespair payment = new ForceOfDespair();
        harness.setHand(player1, List.of(payment, new ForceOfDespair()));
        harness.forceActivePlayer(player2);

        harness.castInstantWithAlternateExileFromHand(player1, 1, (java.util.UUID) null, 0);
        harness.passBothPriorities();

        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId()).containsExactly(payment.getId());
        harness.assertInGraveyard(player1, "Force of Despair");
    }

    private void castNormally() {
        harness.castFromHand(player1, new ForceOfDespair(), "{1}{B}{B}");
        harness.passBothPriorities();
    }
}
