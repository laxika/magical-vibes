package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AvenWarhawk;
import com.github.laxika.magicalvibes.cards.e.EnormousBaloth;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DaruStinger.class, AvenWarhawk.class, EnormousBaloth.class})
class DaruStingerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter for each Soldier card in your hand")
    void entersWithCountersForSoldiersInHand() {
        DaruStinger card = new DaruStinger();
        harness.setHand(player1, List.of(
                card, new AvenWarhawk(), new AvenWarhawk(), new EnormousBaloth()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, gd.interaction
                .activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class).validCardIds());

        assertThat(findPermanent(player1, "Daru Stinger").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Lets its controller choose how many Soldier cards to reveal for Amplify")
    void choosesHowManySoldierCardsToReveal() {
        DaruStinger card = new DaruStinger();
        AvenWarhawk firstSoldier = new AvenWarhawk();
        AvenWarhawk secondSoldier = new AvenWarhawk();
        harness.setHand(player1, List.of(card, firstSoldier, secondSoldier));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(firstSoldier.getId(), secondSoldier.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstSoldier.getId()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Daru Stinger")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals damage equal to its +1/+1 counters to an attacking creature")
    void dealsDamageToAttackingCreature() {
        Permanent stinger = addCreatureReady(player1, new DaruStinger());
        stinger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent attacker = addCreatureReady(player2, new EnormousBaloth());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        assertThat(stinger.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Deals damage equal to its +1/+1 counters to a blocking creature")
    void dealsDamageToBlockingCreature() {
        Permanent stinger = addCreatureReady(player1, new DaruStinger());
        stinger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent blocker = addCreatureReady(player2, new EnormousBaloth());
        blocker.setBlocking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        addCreatureReady(player1, new DaruStinger());
        Permanent bystander = addCreatureReady(player2, new EnormousBaloth());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }

}
