package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NajalTheStormRunner.class, Divination.class, LightningBolt.class})
class NajalTheStormRunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Allows its controller to cast sorceries at instant speed")
    void grantsFlashToControllerSorceries() {
        addCreatureReady(player1, new NajalTheStormRunner());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.passPriority(player2);

        harness.castSorcery(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Divination");
    }

    @Test
    @DisplayName("On attack, paying {2} copies the next instant or sorcery spell this turn")
    void paysToCopyNextInstantOrSorcery() {
        addCreatureReady(player1, new NajalTheStormRunner());
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(9);
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount).doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("Cannot cast a sorcery at instant speed without Najal")
    void sorceryRequiresFlashPermissionWithoutNajal() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void doesNotGrantFlashToOpponentsSorceries() {
        addCreatureReady(player1, new NajalTheStormRunner());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void doesNotGrantFlashToCreatureSpells() {
        addCreatureReady(player1, new NajalTheStormRunner());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new NajalTheStormRunner()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void unusedCopyPermissionExpiresAtEndOfTurn() {
        addCreatureReady(player1, new NajalTheStormRunner());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.setLibrary(player1, List.of(new NajalTheStormRunner(), new NajalTheStormRunner()));
        harness.setLibrary(player2, List.of(new NajalTheStormRunner(), new NajalTheStormRunner()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.passPriority(player2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 17);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void decliningPaymentDoesNotCopySpell() {
        addCreatureReady(player1, new NajalTheStormRunner());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 17);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copiesOnlyFirstSorceryEvenAfterNajalLeavesBattlefield() {
        var najal = addCreatureReady(player1, new NajalTheStormRunner());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        gd.playerBattlefields.get(player1.getId()).remove(najal);
        harness.setGraveyard(player1, List.of(najal.getCard()));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLibrary(player1, List.of(new NajalTheStormRunner(), new NajalTheStormRunner(),
                new NajalTheStormRunner(), new NajalTheStormRunner(), new NajalTheStormRunner(),
                new NajalTheStormRunner(), new NajalTheStormRunner()));
        harness.setHand(player1, List.of(new Divination(), new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gd.stack).isEmpty();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copiedInstantCanChooseNewTargetWithoutChangingOriginal() {
        addCreatureReady(player1, new NajalTheStormRunner());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        assertThat(gd.stack).isEmpty();
    }
}
