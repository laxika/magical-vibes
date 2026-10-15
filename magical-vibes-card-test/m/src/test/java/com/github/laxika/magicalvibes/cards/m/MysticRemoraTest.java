package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MysticRemora.class, IcyManipulator.class, BalduvianBears.class, Island.class})
class MysticRemoraTest extends BaseCardTest {

    @Test
    @DisplayName("Paying cumulative upkeep keeps Mystic Remora")
    void paysCumulativeUpkeep() {
        Permanent remora = harness.addToBattlefieldAndReturn(player1, new MysticRemora());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(remora.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(remora);
    }

    @Test
    @DisplayName("Second cumulative upkeep requires two mana")
    void secondUpkeepRequiresTwoMana() {
        Permanent remora = harness.addToBattlefieldAndReturn(player1, new MysticRemora());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(remora.getCounterCount(CounterType.AGE)).isEqualTo(2);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(remora);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Mystic Remora")
    void declineSacrifices() {
        Permanent remora = harness.addToBattlefieldAndReturn(player1, new MysticRemora());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(remora);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(remora.getCard());
    }

    @Test
    @DisplayName("Triggers when opponent casts a noncreature spell")
    void triggersOnOpponentNoncreatureSpell() {
        Permanent remora = harness.addToBattlefieldAndReturn(player1, new MysticRemora());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new IcyManipulator(), "{4}");

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getLast().getCard()).isSameAs(remora.getCard());
    }

    @Test
    @DisplayName("Does NOT trigger when opponent casts a creature spell")
    void doesNotTriggerOnOpponentCreatureSpell() {
        harness.addToBattlefield(player1, new MysticRemora());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new BalduvianBears(), "{1}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Does NOT trigger when controller casts a noncreature spell")
    void doesNotTriggerOnControllerSpell() {
        harness.addToBattlefield(player1, new MysticRemora());
        IcyManipulator spell = new IcyManipulator();
        harness.castFromHand(player1, spell, "{4}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(spell);
    }

    @Test
    @DisplayName("A Remora trigger resolves after Remora leaves the battlefield")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        Permanent remora = harness.addToBattlefieldAndReturn(player1, new MysticRemora());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new IcyManipulator()));
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castArtifact(player2, 0);
        gd.playerBattlefields.get(player1.getId()).remove(remora);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Opponent with mana is prompted to pay")
    void opponentWithManaIsPrompted() {
        setupOpponentCastsNoncreatureWithMana();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Opponent pays — no draw offer")
    void opponentPaysNoDraw() {
        setupOpponentCastsNoncreatureWithMana();
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("Opponent declines — controller is offered a draw and may accept")
    void opponentDeclinesControllerDraws() {
        setupOpponentCastsNoncreatureWithMana();
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Opponent declines — controller may decline the draw")
    void opponentDeclinesControllerDeclinesDraw() {
        setupOpponentCastsNoncreatureWithMana();
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Can't pay — controller is offered a draw immediately")
    void cantPayOffersDrawImmediately() {
        harness.addToBattlefield(player1, new MysticRemora());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new IcyManipulator()));
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Opponent can generate mana while resolving Remora's payment choice")
    void opponentCanPayUsingUntappedLands() {
        harness.addToBattlefield(player1, new MysticRemora());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new Island());
        }
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castFromHand(player2, new IcyManipulator(), "{4}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        for (int i = 0; i < 4; i++) {
            harness.tapPermanent(player2, i);
        }
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Cumulative upkeep does not trigger during the opponent's upkeep")
    void opponentUpkeepDoesNotAddAgeCounter() {
        Permanent remora = harness.addToBattlefieldAndReturn(player1, new MysticRemora());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(remora.getCounterCount(CounterType.AGE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(remora);
    }

    @Test
    @DisplayName("Cumulative upkeep counts existing age counters and spends the full payment")
    void upkeepCountsExistingAgeCounters() {
        Permanent remora = harness.addToBattlefieldAndReturn(player1, new MysticRemora());
        remora.setCounterCount(CounterType.AGE, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(remora.getCounterCount(CounterType.AGE)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(remora);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Each Remora requires a separate payment for the same spell")
    void multipleRemorasRequireSeparatePayments() {
        harness.addToBattlefield(player1, new MysticRemora());
        setupOpponentCastsNoncreatureWithMana();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    private void setupOpponentCastsNoncreatureWithMana() {
        harness.addToBattlefield(player1, new MysticRemora());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new IcyManipulator()));
        harness.addMana(player2, ManaColor.COLORLESS, 8);

        harness.castArtifact(player2, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }
}
