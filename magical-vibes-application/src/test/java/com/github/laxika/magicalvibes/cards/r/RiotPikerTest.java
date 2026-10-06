package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.k.KraulWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiotPiker.class, KraulWarrior.class})
class RiotPikerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Riot Piker puts it on the battlefield")
    void castingAndResolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new RiotPiker()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Riot Piker");
    }

    @Test
    @DisplayName("Declaring no attackers while Riot Piker can attack throws exception")
    void mustAttackWhenAble() {
        addCreatureReady(player1, new RiotPiker());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Omitting Riot Piker from attackers while declaring other creatures throws exception")
    void mustBeIncludedAmongAttackers() {
        addCreatureReady(player1, new RiotPiker());

        addCreatureReady(player1, new KraulWarrior());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Riot Piker does not need to attack with summoning sickness")
    void doesNotAttackWithSummoningSickness() {
        harness.setLife(player2, 20);

        harness.addToBattlefield(player1, new RiotPiker());

        addCreatureReady(player1, new KraulWarrior());

        declareAttackers(player1, List.of(1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Riot Piker deals 2 combat damage when unblocked")
    void dealsTwoDamageUnblocked() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new RiotPiker());

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A tapped Riot Piker is not required to attack")
    void tappedPikerDoesNotHaveToAttack() {
        Permanent piker = addCreatureReady(player1, new RiotPiker());
        piker.tap();
        addCreatureReady(player1, new KraulWarrior());
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(1));

        harness.assertLife(player2, 18);
        assertThat(piker.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("First strike kills a blocker before it can damage Riot Piker")
    void firstStrikeKillsBlockerBeforeItDealsDamage() {
        Permanent piker = addCreatureReady(player1, new RiotPiker());
        addCreatureReady(player2, new KraulWarrior());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Riot Piker");
        harness.assertInGraveyard(player2, "Kraul Warrior");
        assertThat(piker.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }
}
