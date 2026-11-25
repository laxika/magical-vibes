package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DoubleMajor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SiegeWurm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChorusOfTheConclave.class, Forest.class, SiegeWurm.class, DoubleMajor.class})
class ChorusOfTheConclaveTest extends BaseCardTest {

    @Test
    @DisplayName("Its controller's creature spell enters with a +1/+1 counter for each mana paid")
    void givesItsControllersCreatureSpellAdditionalCountersForManaPaid() {
        harness.addToBattlefield(player2, new ChorusOfTheConclave());
        SiegeWurm creatureSpell = new SiegeWurm();
        castWurmWithChorusPayment(player2, creatureSpell, 2);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creatureSpell.getId())
                        && permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 2);
    }

    @Test
    @DisplayName("An opponent's Chorus offers no additional mana payment for your creature spell")
    void opponentsChorusOffersNoPayment() {
        harness.addToBattlefield(player1, new ChorusOfTheConclave());
        ChorusOfTheConclave creatureSpell = new ChorusOfTheConclave();

        assertThatThrownBy(() -> castCreatureWithChorusPayment(player2, creatureSpell, 2))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getId).contains(creatureSpell.getId());
    }

    @Test
    @DisplayName("Paying zero additional mana is allowed and adds no counters")
    void payingZeroIsAllowed() {
        harness.addToBattlefield(player2, new ChorusOfTheConclave());
        SiegeWurm creatureSpell = new SiegeWurm();
        castWurmWithChorusPayment(player2, creatureSpell, 0);
        harness.passBothPriorities();

        Permanent entered = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creatureSpell.getId()))
                .findFirst().orElseThrow();
        assertThat(entered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Paid counters still arrive if Chorus leaves before the creature spell resolves")
    void paidCountersRemainIfChorusLeavesBeforeCreatureResolves() {
        Permanent chorus = harness.addToBattlefieldAndReturn(player2, new ChorusOfTheConclave());
        SiegeWurm creatureSpell = new SiegeWurm();
        castWurmWithChorusPayment(player2, creatureSpell, 2);

        gd.playerBattlefields.get(player2.getId()).remove(chorus);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creatureSpell.getId())
                        && permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 2);
    }

    @Test
    @DisplayName("Can be blocked when the defending player controls no Forest")
    void forestwalkAllowsBlockingWhenDefenderControlsNoForest() {
        Permanent attacker = addCreatureReady(player1, new ChorusOfTheConclave());
        Permanent blocker = addCreatureReady(player2, new ChorusOfTheConclave());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Can't be blocked when the defending player controls a Forest")
    void forestwalkPreventsBlockingWhenDefenderControlsAForest() {
        Permanent attacker = addCreatureReady(player1, new ChorusOfTheConclave());
        Permanent blocker = addCreatureReady(player2, new ChorusOfTheConclave());
        harness.addToBattlefield(player2, new Forest());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Its controller can pay for their own creature spell")
    void controllerCanPayForItsOwnCreatureSpell() {
        harness.addToBattlefield(player1, new ChorusOfTheConclave());
        SiegeWurm wurm = new SiegeWurm();
        harness.setHand(player1, List.of(wurm));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{1}", "{1}", "{1}"));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(wurm.getId())
                        && permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Convoke can pay the additional mana")
    void convokeCanPayTheAdditionalCost() {
        Permanent chorus = harness.addToBattlefieldAndReturn(player1, new ChorusOfTheConclave());
        SiegeWurm wurm = new SiegeWurm();
        harness.setHand(player1, List.of(wurm));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(chorus.getId()), false, null, null, List.of(), null, null,
                false, null, null, List.of(), List.of(), List.of("{1}"), false);
        harness.passBothPriorities();

        assertThat(chorus.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(wurm.getId())
                        && permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("A copy of the creature spell enters with the paid counters too")
    void copiedCreatureSpellRetainsCountersFromPaidAdditionalCost() {
        harness.addToBattlefield(player1, new ChorusOfTheConclave());
        SiegeWurm wurm = new SiegeWurm();
        harness.setHand(player1, List.of(wurm, new DoubleMajor()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{1}", "{1}"));
        harness.castInstant(player1, 0, wurm.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(wurm.getId())
                        && permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 2);
    }

    private void castCreatureWithChorusPayment(Player player, Card card, int additionalGenericMana) {
        harness.setHand(player, List.of(card));
        addManaForChorus(player, additionalGenericMana);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        List<String> repeatedAdditionalCosts = java.util.Collections.nCopies(additionalGenericMana, "{1}");
        harness.castCreatureWithRepeatedCosts(player, 0, repeatedAdditionalCosts);
    }

    private void castWurmWithChorusPayment(Player player, Card card, int additionalGenericMana) {
        harness.setHand(player, List.of(card));
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.addMana(player, ManaColor.COLORLESS, 5 + additionalGenericMana);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreatureWithRepeatedCosts(player, 0,
                java.util.Collections.nCopies(additionalGenericMana, "{1}"));
    }

    private void addManaForChorus(Player player, int additionalGenericMana) {
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 4 + additionalGenericMana);
    }
}
