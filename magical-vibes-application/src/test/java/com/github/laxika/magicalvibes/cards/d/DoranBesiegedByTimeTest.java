package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
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

@CardUsed({DoranBesiegedByTime.class, GiantSpider.class, GoblinPiker.class, DawnhandDissident.class})
class DoranBesiegedByTimeTest extends BaseCardTest {

    @Test
    @DisplayName("Creature spells with greater toughness cost {1} less to cast")
    void reducesCreatureSpellsWithGreaterToughness() {
        harness.addToBattlefield(player1, new DoranBesiegedByTime());
        harness.setHand(player1, List.of(new GiantSpider()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Creature spells without greater toughness are not reduced")
    void doesNotReduceCreatureSpellsWithoutGreaterToughness() {
        harness.addToBattlefield(player1, new DoranBesiegedByTime());
        harness.setHand(player1, List.of(new GoblinPiker()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An attacking creature you control gets +X/+X for its power-toughness difference")
    void boostsAttackingCreatureYouControl() {
        harness.addToBattlefield(player1, new DoranBesiegedByTime());
        Permanent attacker = addReadyCreature(player1, new GiantSpider());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(4);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("A blocking creature you control gets +X/+X for its power-toughness difference")
    void boostsBlockingCreatureYouControl() {
        harness.addToBattlefield(player1, new DoranBesiegedByTime());
        Permanent blocker = addReadyCreature(player1, new GiantSpider());
        Permanent attacker = addReadyCreature(player2, new GoblinPiker());
        attacker.setAttacking(true);

        declareBlockers(List.of(new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(4);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("A creature controlled by an opponent does not trigger Doran when it blocks")
    void opponentBlockingCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new DoranBesiegedByTime());
        Permanent attacker = addReadyCreature(player1, new GoblinPiker());
        Permanent blocker = addReadyCreature(player2, new GiantSpider());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(gd.stack).isEmpty();
        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void boostsItselfWhenAttackingAndExpiresAtEndOfTurn() {
        Permanent doran = addReadyCreature(player1, new DoranBesiegedByTime());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(doran.getEffectivePower()).isEqualTo(5);
        assertThat(doran.getEffectiveToughness()).isEqualTo(10);

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(doran.getEffectivePower()).isZero();
        assertThat(doran.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    void usesActualNegativePowerToCalculateDifference() {
        Permanent doran = addReadyCreature(player1, new DoranBesiegedByTime());
        doran.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(doran.getEffectivePower()).isEqualTo(2);
        assertThat(doran.getEffectiveToughness()).isEqualTo(7);
    }

    @Test
    void calculatesDifferenceAtResolution() {
        Permanent doran = addReadyCreature(player1, new DoranBesiegedByTime());

        declareAttackers(List.of(0));
        doran.setPowerModifier(2);
        harness.passBothPriorities();

        assertThat(doran.getEffectivePower()).isEqualTo(5);
        assertThat(doran.getEffectiveToughness()).isEqualTo(8);
    }

    @Test
    void boostsCreatureWithPowerGreaterThanToughness() {
        harness.addToBattlefield(player1, new DoranBesiegedByTime());
        Permanent attacker = addReadyCreature(player1, new GoblinPiker());

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(3);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void opponentAttackingCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new DoranBesiegedByTime());
        Permanent attacker = addReadyCreature(player2, new GiantSpider());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void doesNotReduceOpponentsCreatureSpells() {
        harness.addToBattlefield(player1, new DoranBesiegedByTime());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantSpider()));
        harness.addMana(player2, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotReduceColoredManaCosts() {
        harness.addToBattlefield(player1, new DoranBesiegedByTime());
        harness.setHand(player1, List.of(new DawnhandDissident()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void declareBlockers(List<BlockerAssignment> assignments) {
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, assignments);
    }
}
