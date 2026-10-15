package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RelentlessAssault;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MichelangeloTheHeart.class, GrizzlyBears.class, Forest.class, RelentlessAssault.class})
class MichelangeloTheHeartTest extends BaseCardTest {

    @Test
    void raidPutsCounterOnTargetCreatureAndCreatesFoodAtPostcombatMain() {
        harness.addToBattlefield(player1, new MichelangeloTheHeart());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        advanceToPostcombatMain(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    void doesNotTriggerWithoutRaid() {
        harness.addToBattlefield(player1, new MichelangeloTheHeart());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToPostcombatMain(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    void raidCanTargetAnyCreatureButNotNoncreatures() {
        harness.addToBattlefield(player1, new MichelangeloTheHeart());
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        advanceToPostcombatMain(player1);

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownTarget.getId()).doesNotContain(ownForest.getId());
        harness.handlePermanentChosen(player1, ownTarget.getId());
        harness.passBothPriorities();

        assertThat(ownTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerDuringOpponentsSecondMainPhase() {
        harness.addToBattlefield(player1, new MichelangeloTheHeart());
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());

        advanceToPostcombatMain(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    void illegalTargetPreventsFoodCreationToo() {
        harness.addToBattlefield(player1, new MichelangeloTheHeart());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        advanceToPostcombatMain(player1);
        harness.handlePermanentChosen(player1, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Food");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void createdFoodCanBeSacrificedForThreeLife() {
        Permanent michelangelo = harness.addToBattlefieldAndReturn(player1, new MichelangeloTheHeart());
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        advanceToPostcombatMain(player1);
        harness.handlePermanentChosen(player1, michelangelo.getId());
        harness.passBothPriorities();
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(
                gd.playerBattlefields.get(player1.getId()).stream()
                        .filter(permanent -> permanent.getCard().getName().equals("Food"))
                        .findFirst().orElseThrow());

        harness.activateAbility(player1, foodIndex, null, null);

        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertLife(player1, 10);
        harness.passBothPriorities();
        harness.assertLife(player1, 13);
    }

    @Test
    void doesNotTriggerAgainAtThirdMainPhase() {
        Permanent michelangelo = harness.addToBattlefieldAndReturn(player1, new MichelangeloTheHeart());
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        advanceToPostcombatMain(player1);
        harness.handlePermanentChosen(player1, michelangelo.getId());
        harness.passBothPriorities();

        harness.castFromHand(player1, new RelentlessAssault(), "{2}{R}{R}");
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(michelangelo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Food"))).hasSize(1);
    }

    private void advanceToPostcombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntil(activePlayer, TurnStep.POSTCOMBAT_MAIN);
    }
}
