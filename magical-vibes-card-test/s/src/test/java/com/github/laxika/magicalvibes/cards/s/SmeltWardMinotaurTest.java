package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SmeltWardMinotaur.class, GrizzlyBears.class, Shock.class, Divination.class, SureStrike.class})
class SmeltWardMinotaurTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant makes a chosen opponent creature unable to block")
    void castingInstantRestrictsChosenOpponentCreature() {
        harness.addToBattlefield(player1, new SmeltWardMinotaur());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(opponentCreature.getId())
                .doesNotContain(ownCreature.getId());

        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isCantBlockThisTurn()).isTrue();
        assertThat(ownCreature.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Casting a sorcery also triggers Smelt-Ward Minotaur")
    void castingSorceryRestrictsOpponentCreature() {
        harness.addToBattlefield(player1, new SmeltWardMinotaur());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Casting a creature does not trigger Smelt-Ward Minotaur")
    void castingCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new SmeltWardMinotaur());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The blocking restriction wears off at end of turn")
    void restrictionWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new SmeltWardMinotaur());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        resolveAllTriggers();
        assertThat(opponentCreature.isCantBlockThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(opponentCreature.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("An opponent's instant does not trigger the Minotaur")
    void opponentsInstantDoesNotTrigger() {
        Permanent minotaur = addCreatureReady(player1, new SmeltWardMinotaur());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new SureStrike()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, opponentCreature.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(minotaur.isCantBlockThisTurn()).isFalse();
        assertThat(opponentCreature.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("A spell can resolve when the Minotaur trigger has no legal target")
    void noOpponentCreatureDoesNotPreventSpellResolving() {
        Permanent minotaur = addCreatureReady(player1, new SmeltWardMinotaur());
        harness.setHand(player1, List.of(new SureStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, minotaur.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        resolveAllTriggers();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof SureStrike);
        assertThat(minotaur.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("A triggered ability still resolves after its source leaves the battlefield")
    void triggerResolvesAfterSourceLeaves() {
        Permanent minotaur = addCreatureReady(player1, new SmeltWardMinotaur());
        Permanent opponentCreature = addCreatureReady(player2, new SmeltWardMinotaur());
        Permanent otherOpponentCreature = addCreatureReady(player2, new SmeltWardMinotaur());
        harness.setHand(player1, List.of(new SureStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, opponentCreature.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(minotaur);
        gd.playerGraveyards.get(player1.getId()).add(minotaur.getCard());
        resolveAllTriggers();

        assertThat(opponentCreature.isCantBlockThisTurn()).isTrue();
        assertThat(otherOpponentCreature.isCantBlockThisTurn()).isFalse();
    }
}
