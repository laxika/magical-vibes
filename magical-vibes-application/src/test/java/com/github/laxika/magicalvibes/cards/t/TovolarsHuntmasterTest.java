package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({TovolarsHuntmaster.class, GrizzlyBears.class})
class TovolarsHuntmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two 2/2 green Wolf tokens")
    void entersWithTwoWolves() {
        harness.setHand(player1, List.of(new TovolarsHuntmaster()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Wolf")).hasSize(2);
        assertThat(findPermanents(player1, "Wolf")).allMatch(permanent ->
                permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.WOLF));
    }

    @Test
    @DisplayName("Transforms into Tovolar's Packleader when no spells were cast last turn")
    void transformsWhenNoSpellsWereCast() {
        Permanent huntmaster = addCreatureReady(player1, new TovolarsHuntmaster());
        gd.spellsCastLastTurn.clear();

        advanceToUpkeepAndResolve(player1);

        assertThat(huntmaster.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Transforms back when a player cast two or more spells last turn")
    void transformsBackWhenTwoSpellsWereCast() {
        Permanent packleader = addTransformedPackleader(player1);
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        advanceToUpkeepAndResolve(player2);

        assertThat(packleader.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Creates two Wolves whenever the Packleader attacks")
    void attackingCreatesTwoWolves() {
        addTransformedPackleader(player1);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Wolf")).hasSize(2);
    }

    @Test
    @DisplayName("Another Wolf or Werewolf fights a creature the controller does not control")
    void anotherWolfOrWerewolfFightsOpponentCreature() {
        Permanent packleader = addTransformedPackleader(player1);
        Permanent otherWerewolf = addCreatureReady(player1, new TovolarsHuntmaster());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbilityWithMultiTargets(
                player1,
                0,
                0,
                List.of(otherWerewolf.getId(), opponentCreature.getId()));
        harness.passBothPriorities();

        assertThat(otherWerewolf.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
        assertThat(packleader.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The Packleader cannot target itself as the other Wolf or Werewolf")
    void cannotTargetItselfAsFirstTarget() {
        Permanent packleader = addTransformedPackleader(player1);
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1,
                0,
                0,
                List.of(packleader.getId(), opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addTransformedPackleader(Player player) {
        harness.addToBattlefield(player, new TovolarsHuntmaster());
        Permanent packleader = findPermanent(player, "Tovolar's Huntmaster");
        packleader.setCard(packleader.getOriginalCard().getBackFaceCard());
        packleader.setTransformed(true);
        packleader.setSummoningSick(false);
        return packleader;
    }

    private void advanceToUpkeepAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
