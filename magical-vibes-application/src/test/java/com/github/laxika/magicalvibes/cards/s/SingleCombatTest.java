package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChandrasPyrohelix;
import com.github.laxika.magicalvibes.cards.g.GideonBlackblade;
import com.github.laxika.magicalvibes.cards.n.NarsetParterOfVeils;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.TamiyoCollectorOfTales;
import com.github.laxika.magicalvibes.cards.t.TurretOgre;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SingleCombat.class, Snarespinner.class, TurretOgre.class, NarsetParterOfVeils.class,
        Plains.class, ChandrasPyrohelix.class, GideonBlackblade.class, TamiyoCollectorOfTales.class})
class SingleCombatTest extends BaseCardTest {

    @Test
    @DisplayName("Each player keeps one creature or planeswalker and sacrifices the rest")
    void eachPlayerKeepsOneCreatureOrPlaneswalker() {
        Permanent keptCreature = addCreature(player1, new Snarespinner());
        Permanent sacrificedCreature = addCreature(player1, new TurretOgre());
        Permanent keptPlaneswalker = harness.addToBattlefieldAndReturn(player2, new NarsetParterOfVeils());
        keptPlaneswalker.setCounterCount(CounterType.LOYALTY, 5);
        Permanent sacrificedCreatureOpponent = addCreature(player2, new Snarespinner());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Plains());
        harness.forceActivePlayer(player1);

        cast();

        PendingInteraction.MultiPermanentChoice player1Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player1Choice).isNotNull();
        assertThat(player1Choice.playerId()).isEqualTo(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(sacrificedCreature.getId()));

        PendingInteraction.MultiPermanentChoice player2Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player2Choice).isNotNull();
        assertThat(player2Choice.playerId()).isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(sacrificedCreatureOpponent.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(keptCreature)
                .doesNotContain(sacrificedCreature);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(keptPlaneswalker)
                .doesNotContain(sacrificedCreatureOpponent);
        harness.assertOnBattlefield(player1, "Plains");
        harness.assertOnBattlefield(player2, "Plains");
        harness.assertInGraveyard(player1, "Turret Ogre");
        harness.assertInGraveyard(player2, "Snarespinner");
    }

    @Test
    @DisplayName("Creature and planeswalker spells are forbidden while other spells remain castable")
    void restrictsCreatureAndPlaneswalkerSpells() {
        addCreature(player1, new Snarespinner());
        addCreature(player2, new Snarespinner());
        cast();

        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new Snarespinner()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player2, List.of(new NarsetParterOfVeils()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        assertThatThrownBy(() -> harness.castPlaneswalker(player2, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player2, List.of(new ChandrasPyrohelix()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, Map.of(player1.getId(), 2));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The restriction lasts through the caster's next turn and ends at that turn's end")
    void restrictionEndsAtEndOfCastersNextTurn() {
        addCreature(player1, new Snarespinner());
        addCreature(player2, new Snarespinner());
        cast();

        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new Snarespinner()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Snarespinner()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Snarespinner()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Snarespinner");
    }

    @Test
    void sacrificesWaitUntilBothPlayersHaveChosenAndRestrictionResumesAfterward() {
        Permanent keptCreature = addCreature(player1, new Snarespinner());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new NarsetParterOfVeils());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        Permanent ogre = addCreature(player1, new TurretOgre());
        Permanent opponentKept = addCreature(player2, new Snarespinner());
        Permanent opponentSacrifice = addCreature(player2, new TurretOgre());

        cast();
        harness.handleMultiplePermanentsChosen(player1, List.of(planeswalker.getId(), ogre.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(keptCreature, planeswalker, ogre);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentKept, opponentSacrifice);

        harness.handleMultiplePermanentsChosen(player2, List.of(opponentSacrifice.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(keptCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opponentKept);
        harness.assertInGraveyard(player1, "Narset, Parter of Veils");
        harness.assertInGraveyard(player1, "Turret Ogre");
        harness.setHand(player1, List.of(new Snarespinner()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void emptyBattlefieldsStillCreateCastingRestriction() {
        cast();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.setHand(player1, List.of(new Snarespinner()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.setHand(player1, List.of(new NarsetParterOfVeils()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        assertThatThrownBy(() -> harness.castPlaneswalker(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sacrificePreventionPreservesOpponentsPermanentsButDoesNotPreventCastingRestriction() {
        Permanent tamiyo = harness.addToBattlefieldAndReturn(player2, new TamiyoCollectorOfTales());
        tamiyo.setCounterCount(CounterType.LOYALTY, 5);
        Permanent opponentCreature = addCreature(player2, new Snarespinner());
        Permanent ownTamiyo = harness.addToBattlefieldAndReturn(player1, new TamiyoCollectorOfTales());
        ownTamiyo.setCounterCount(CounterType.LOYALTY, 5);
        Permanent ownCreature = addCreature(player1, new Snarespinner());

        cast();
        harness.handleMultiplePermanentsChosen(player1, List.of(ownCreature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(ownTamiyo);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(tamiyo, opponentCreature);
        harness.assertInGraveyard(player1, "Snarespinner");
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new Snarespinner()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creaturePlaneswalkerCountsOnceAndIndestructibleDoesNotPreventSacrifice() {
        harness.forceActivePlayer(player1);
        Permanent gideon = harness.addToBattlefieldAndReturn(player1, new GideonBlackblade());
        gideon.setCounterCount(CounterType.LOYALTY, 4);
        Permanent creature = addCreature(player1, new Snarespinner());

        cast();
        harness.handleMultiplePermanentsChosen(player1, List.of(gideon.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(creature);
        harness.assertInGraveyard(player1, "Gideon Blackblade");
    }

    private void cast() {
        harness.setHand(player1, List.of(new SingleCombat()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private Permanent addCreature(Player player, Card card) {
        return harness.addToBattlefieldAndReturn(player, card);
    }

    private void prepareMainPhase(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
