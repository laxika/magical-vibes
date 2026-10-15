package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ActOfTreason;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.y.YoshimaruEverFaithful;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({TeveshSzatDoomOfFools.class, YoshimaruEverFaithful.class, JaceBeleren.class, ActOfTreason.class})
class TeveshSzatDoomOfFoolsTest extends BaseCardTest {

    @Test
    @DisplayName("+2 creates two Thrulls")
    void plusTwoCreatesThrulls() {
        Permanent tevesh = addReadyTevesh(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(tevesh.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(findPermanents(player1, "Thrull")).hasSize(2).allSatisfy(thrull -> {
            assertThat(thrull.getCard().isToken()).isTrue();
            assertThat(gqs.getEffectivePower(gd, thrull)).isZero();
            assertThat(gqs.getEffectiveToughness(gd, thrull)).isEqualTo(1);
            assertThat(gqs.getEffectiveColors(gd, thrull)).containsExactly(CardColor.BLACK);
            assertThat(thrull.getCard().getSubtypes()).contains(CardSubtype.THRULL);
        });
    }

    @Test
    @DisplayName("+1 can sacrifice a commander and draw three cards")
    void plusOneSacrificesCommanderAndDrawsThree() {
        Permanent tevesh = addReadyTevesh(player1);
        Permanent commander = addCreatureReady(player1, new YoshimaruEverFaithful());
        commander.setCommander(true);
        harness.setLibrary(player1, List.of(new YoshimaruEverFaithful(), new YoshimaruEverFaithful(), new YoshimaruEverFaithful()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, commander.getId());
        harness.passBothPriorities();

        assertThat(tevesh.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(commander);
    }

    @Test
    @DisplayName("-10 takes battlefield commanders and puts command-zone commanders onto the battlefield")
    void minusTenTakesAndReturnsCommanders() {
        Permanent tevesh = addReadyTevesh(player1);
        tevesh.setCounterCount(CounterType.LOYALTY, 10);
        Permanent battlefieldCommander = addCreatureReady(player2, new YoshimaruEverFaithful());
        battlefieldCommander.setCommander(true);
        Card commandZoneCommander = new TeveshSzatDoomOfFools();
        gd.playerCommandZones.get(player2.getId()).add(commandZoneCommander);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerCommandZones.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(battlefieldCommander)
                .anyMatch(permanent -> permanent.getCard().getId().equals(commandZoneCommander.getId())
                        && permanent.isCommander());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(battlefieldCommander);
    }

    @Test
    @DisplayName("+1 sacrifices a noncommander creature and draws exactly two cards")
    void plusOneSacrificesNoncommander() {
        addReadyTevesh(player1);
        Permanent creature = addCreatureReady(player1, new YoshimaruEverFaithful());
        harness.setLibrary(player1, List.of(new YoshimaruEverFaithful(), new YoshimaruEverFaithful(), new YoshimaruEverFaithful()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("+1 draws during resolution after sacrificing another planeswalker")
    void plusOneDrawsWithoutSeparateTrigger() {
        addReadyTevesh(player1);
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        harness.setLibrary(player1, List.of(new YoshimaruEverFaithful(), new YoshimaruEverFaithful(), new YoshimaruEverFaithful()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, jace.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(jace);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining +1 keeps the creature and draws nothing")
    void plusOneMayBeDeclined() {
        Permanent tevesh = addReadyTevesh(player1);
        Permanent creature = addCreatureReady(player1, new YoshimaruEverFaithful());
        harness.setLibrary(player1, List.of(new YoshimaruEverFaithful(), new YoshimaruEverFaithful()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(tevesh.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("+1 cannot sacrifice Tevesh himself or an opponent's creature")
    void plusOneHasNoEligibleSacrifice() {
        Permanent tevesh = addReadyTevesh(player1);
        Permanent opposingCreature = addCreatureReady(player2, new YoshimaruEverFaithful());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(tevesh);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingCreature);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(tevesh.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("-10 recognizes a designated commander entering through the normal battlefield path")
    void minusTenRecognizesRegisteredCommander() {
        Permanent tevesh = addReadyTevesh(player1);
        tevesh.setCounterCount(CounterType.LOYALTY, 11);
        Card commanderCard = new YoshimaruEverFaithful();
        gd.playerCommanders.put(player2.getId(), List.of(commanderCard));
        Permanent commander = harness.enterBattlefieldAndReturn(player2, commanderCard);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(commander);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(commander);
    }

    @Test
    @DisplayName("-10 permanently keeps an opponent's commander already borrowed until end of turn")
    void minusTenKeepsTemporarilyControlledCommander() {
        Permanent tevesh = addReadyTevesh(player1);
        tevesh.setCounterCount(CounterType.LOYALTY, 11);
        Permanent commander = addCreatureReady(player2, new YoshimaruEverFaithful());
        commander.setCommander(true);
        harness.setHand(player1, List.of(new ActOfTreason()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, commander.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(commander);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(commander);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(commander);
    }

    private Permanent addReadyTevesh(Player player) {
        Permanent tevesh = addCreatureReady(player, new TeveshSzatDoomOfFools());
        tevesh.setCounterCount(CounterType.LOYALTY, 4);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return tevesh;
    }
}
