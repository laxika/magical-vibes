package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KaitoDancingShadowTest extends BaseCardTest {

    @Test
    void combatDamageMayReturnTheDealerAndAllowTwoLoyaltyActivations() {
        Permanent kaito = addReadyKaito(3);
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        Permanent secondAttacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        secondAttacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, secondAttacker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(kaito.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void plusOneLocksCreatureFromAttackingAndBlockingUntilNextTurn() {
        addReadyKaito(3);
        Permanent creature = addReadyCreature(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, creature.getId(), null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(1)))
                .isInstanceOf(IllegalStateException.class);

        Permanent attacker = addReadyCreature(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void zeroDrawsACard() {
        addReadyKaito(3);
        harness.setLibrary(player1, List.of(new Shock()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Shock");
    }

    @Test
    void minusTwoCreatesDroneThatDrainsWhenItLeaves() {
        addReadyKaito(3);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        Permanent drone = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(drone.getCard().getColor()).isNull();
        assertThat(drone.getCard().getSubtypes()).contains(CardSubtype.DRONE);
        assertThat(drone.getCard().getKeywords()).contains(Keyword.DEATHTOUCH);
        assertThat(gqs.getEffectivePower(gd, drone)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, drone)).isEqualTo(2);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castInstant(player2, 0, drone.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private Permanent addReadyKaito(int loyalty) {
        Permanent kaito = new Permanent(new KaitoDancingShadow());
        kaito.setCounterCount(CounterType.LOYALTY, loyalty);
        kaito.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(kaito);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return kaito;
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent creature = new Permanent(card);
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }
}
