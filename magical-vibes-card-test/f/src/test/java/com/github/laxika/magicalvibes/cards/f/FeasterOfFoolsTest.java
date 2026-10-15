package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ChangelingOutcast;
import com.github.laxika.magicalvibes.cards.m.MotherBear;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FeasterOfFools.class, MotherBear.class, ChangelingOutcast.class})
class FeasterOfFoolsTest extends BaseCardTest {

    @Test
    @DisplayName("Devouring two creatures gives Feaster of Fools four +1/+1 counters")
    void devourTwoAddsFourCounters() {
        Permanent fodderA = harness.addToBattlefieldAndReturn(player1, new MotherBear());
        Permanent fodderB = harness.addToBattlefieldAndReturn(player1, new MotherBear());

        castFeaster();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(fodderA.getId(), fodderB.getId()));

        Permanent feaster = findPermanent(player1, "Feaster of Fools");
        assertThat(feaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining to devour leaves Feaster of Fools without counters")
    void devourNoneAddsNoCounters() {
        harness.addToBattlefield(player1, new MotherBear());

        castFeaster();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        Permanent feaster = findPermanent(player1, "Feaster of Fools");
        assertThat(feaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Convoke lets a creature help pay for Feaster of Fools")
    void convokeHelpsCastFeaster() {
        Permanent convokeCreature = harness.addToBattlefieldAndReturn(player1, new MotherBear());
        harness.setHand(player1, List.of(new FeasterOfFools()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(convokeCreature.getId()));

        assertThat(convokeCreature.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of());
        assertThat(findPermanent(player1, "Feaster of Fools")).isNotNull();
    }

    @Test
    @DisplayName("A summoning-sick black creature can convoke black mana and then be devoured")
    void convokedBlackCreatureCanBeDevoured() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new ChangelingOutcast());
        harness.setHand(player1, List.of(new FeasterOfFools()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(fodder.getId()));

        assertThat(fodder.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(fodder.getId()));

        assertThat(findPermanent(player1, "Feaster of Fools").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName()).containsExactly("Changeling Outcast");
    }

    @Test
    @DisplayName("An opponent's creature is not available to devour")
    void noControlledCreaturesEntersWithoutPrompt() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new MotherBear());

        castFeaster();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanent(player1, "Feaster of Fools").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opponentCreature);
    }

    @Test
    @DisplayName("Devour can sacrifice only a subset of the available creatures")
    void devourOneLeavesOtherCreature() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new MotherBear());
        Permanent retained = harness.addToBattlefieldAndReturn(player1, new MotherBear());

        castFeaster();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(sacrificed.getId()));

        Permanent feaster = findPermanent(player1, "Feaster of Fools");
        assertThat(feaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(retained, feaster);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(sacrificed.getCard());
    }

    @Test
    @DisplayName("Flying prevents a ground creature from blocking Feaster of Fools")
    void groundCreatureCannotBlockButFlyingCreatureCan() {
        castFeaster();
        harness.passBothPriorities();
        Permanent attacker = findPermanent(player1, "Feaster of Fools");
        Permanent groundBlocker = harness.addToBattlefieldAndReturn(player2, new MotherBear());
        Permanent flyingBlocker = harness.addToBattlefieldAndReturn(player2, new FeasterOfFools());

        assertThat(bls.canBlockAttacker(gd, groundBlocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, flyingBlocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    private void castFeaster() {
        harness.castFromHand(player1, new FeasterOfFools(), "{4}{B}{B}");
    }
}
