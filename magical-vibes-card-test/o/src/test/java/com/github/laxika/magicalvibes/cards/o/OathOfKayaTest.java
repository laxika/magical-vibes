package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GideonBlackblade;
import com.github.laxika.magicalvibes.cards.g.GoblinAssailant;
import com.github.laxika.magicalvibes.cards.a.AjaniTheGreathearted;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
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
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OathOfKaya.class, GideonBlackblade.class, GoblinAssailant.class, AjaniTheGreathearted.class,
        InvasionOfZendikar.class})
class OathOfKayaTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 3 damage to the target and gains 3 life")
    void etbDealsDamageAndGainsLife() {
        castOath(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Gains 2 life and deals 2 damage once when an opponent attacks a planeswalker")
    void triggersOnceForMultipleAttackers() {
        Permanent planeswalker = addGideon(player1);
        harness.addToBattlefield(player1, new OathOfKaya());
        addReadyCreature(player2);
        addReadyCreature(player2);

        declareAttackers(player2, List.of(0, 1), Map.of(0, planeswalker.getId(), 1, planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not trigger when an opponent attacks the player instead")
    void doesNotTriggerForAttackOnPlayer() {
        harness.addToBattlefield(player1, new OathOfKaya());
        addReadyCreature(player2);

        declareAttackers(player2, List.of(0), null);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggersForEachAttackedPlaneswalker() {
        Permanent gideon = addGideon(player1);
        Permanent ajani = harness.addToBattlefieldAndReturn(player1, new AjaniTheGreathearted());
        ajani.setCounterCount(CounterType.LOYALTY, 5);
        harness.addToBattlefield(player1, new OathOfKaya());
        addReadyCreature(player2);
        addReadyCreature(player2);

        declareAttackers(player2, List.of(0, 1), Map.of(0, gideon.getId(), 1, ajani.getId()));
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }

    @Test
    void etbCanKillCreatureAndGainLife() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GoblinAssailant());

        castOath(creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Goblin Assailant");
        harness.assertLife(player1, 23);
    }

    @Test
    void etbCanDamagePlaneswalkerAndGainLife() {
        Permanent gideon = addGideon(player2);

        castOath(gideon.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertLife(player1, 23);
    }

    @Test
    void etbCanTargetItsController() {
        castOath(player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void etbDoesNotGainLifeWhenItsOnlyTargetLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GoblinAssailant());
        castOath(creature.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerHands.get(player2.getId()).add(creature.getCard());

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Oath of Kaya");
    }

    @Test
    void attackTriggerResolvesAfterOathLeaves() {
        Permanent gideon = addGideon(player1);
        Permanent oath = harness.addToBattlefieldAndReturn(player1, new OathOfKaya());
        addReadyCreature(player2);
        declareAttackers(player2, List.of(0), Map.of(0, gideon.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(oath);
        gd.playerGraveyards.get(player1.getId()).add(oath.getCard());

        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void etbCanTargetBattle() {
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfZendikar());
        battle.setCounterCount(CounterType.DEFENSE, 3);

        castOath(battle.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isZero();
        harness.assertLife(player1, 23);
    }

    private void castOath(UUID targetId) {
        harness.setHand(player1, List.of(new OathOfKaya()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, targetId);
    }

    private Permanent addGideon(Player player) {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player, new GideonBlackblade());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        planeswalker.setSummoningSick(false);
        return planeswalker;
    }

    private void addReadyCreature(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GoblinAssailant());
        creature.setSummoningSick(false);
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, Map<Integer, UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }
}
