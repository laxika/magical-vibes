package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LilianaOfTheVeil;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SingleCombat.class, GrizzlyBears.class, HillGiant.class, LilianaOfTheVeil.class,
        Plains.class, Shock.class})
class SingleCombatTest extends BaseCardTest {

    @Test
    @DisplayName("Each player keeps one creature or planeswalker and sacrifices the rest")
    void eachPlayerKeepsOneCreatureOrPlaneswalker() {
        Permanent keptCreature = addCreature(player1, new GrizzlyBears());
        Permanent sacrificedCreature = addCreature(player1, new HillGiant());
        Permanent keptPlaneswalker = harness.addToBattlefieldAndReturn(player2, new LilianaOfTheVeil());
        keptPlaneswalker.setCounterCount(CounterType.LOYALTY, 3);
        Permanent sacrificedCreatureOpponent = addCreature(player2, new GrizzlyBears());
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
        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Creature and planeswalker spells are forbidden while other spells remain castable")
    void restrictsCreatureAndPlaneswalkerSpells() {
        addCreature(player1, new GrizzlyBears());
        addCreature(player2, new GrizzlyBears());
        cast();

        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player2, List.of(new LilianaOfTheVeil()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        assertThatThrownBy(() -> harness.castPlaneswalker(player2, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The restriction lasts through the caster's next turn and ends at that turn's end")
    void restrictionEndsAtEndOfCastersNextTurn() {
        addCreature(player1, new GrizzlyBears());
        addCreature(player2, new GrizzlyBears());
        cast();

        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void cast() {
        harness.setHand(player1, List.of(new SingleCombat()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
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
