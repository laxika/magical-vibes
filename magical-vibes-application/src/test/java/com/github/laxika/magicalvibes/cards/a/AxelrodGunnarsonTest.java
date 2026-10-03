package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AxelrodGunnarson.class, BarbaryApes.class, ChandraNalaar.class})
class AxelrodGunnarsonTest extends BaseCardTest {

    @Test
    @DisplayName("When a creature it damaged dies, Axelrod gains life and damages the chosen player")
    void triggersWhenDamagedCreatureDies() {
        addAttackingAxelrod();
        Permanent blocker = addBlocker(2, 2);

        passCombatDamage(blocker);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        harness.assertInGraveyard(player2, "Barbary Apes");
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getCard() instanceof AxelrodGunnarson);
    }

    @Test
    @DisplayName("Axelrod's ability triggers if it dies at the same time as the creature it damaged")
    void triggersAfterSimultaneousDeath() {
        Permanent axelrod = addAttackingAxelrod();
        Permanent blocker = addBlocker(5, 5);

        passCombatDamage(blocker);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        harness.assertInGraveyard(player1, "Axelrod Gunnarson");
        harness.assertInGraveyard(player2, "Barbary Apes");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(axelrod);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @CardUsed(ChandraNalaar.class)
    @Test
    @DisplayName("The trigger can target a planeswalker instead of a player")
    void targetsPlaneswalker() {
        addAttackingAxelrod();
        addBlocker(2, 2);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);

        passCombatDamage(findPermanents(player2, "Barbary Apes").getFirst());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("The trigger still fires when a creature it damaged dies later that turn")
    void triggersWhenDamagedCreatureDiesLaterThatTurn() {
        addAttackingAxelrod();
        Permanent blocker = addBlocker(2, 6);

        passCombatDamage(blocker);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(blocker.getMarkedDamage()).isEqualTo(5);

        TestCards.mutableCard(blocker).setToughness(5);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        harness.assertInGraveyard(player2, "Barbary Apes");
    }

    @Test
    @DisplayName("Axelrod can target its controller, gaining life before dealing damage")
    void canTargetItsController() {
        addAttackingAxelrod();
        Permanent blocker = addBlocker(2, 2);

        passCombatDamage(blocker);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Barbary Apes");
    }

    @CardUsed(ChandraNalaar.class)
    @Test
    @DisplayName("No life is gained if the targeted planeswalker leaves before resolution")
    void doesNotGainLifeWhenOnlyTargetBecomesIllegal() {
        addAttackingAxelrod();
        Permanent blocker = addBlocker(2, 2);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);

        passCombatDamage(blocker);
        harness.handlePermanentChosen(player1, planeswalker.getId());

        planeswalker.setCounterCount(CounterType.LOYALTY, 0);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player2, "Chandra Nalaar");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("No trigger occurs if Axelrod leaves before the damaged creature dies")
    void doesNotTriggerAfterAxelrodHasLeft() {
        Permanent axelrod = addAttackingAxelrod();
        Permanent blocker = addBlocker(2, 6);

        passCombatDamage(blocker);
        assertThat(blocker.getMarkedDamage()).isEqualTo(5);
        assertThat(gd.interaction.activeInteraction()).isNull();

        TestCards.mutableCard(axelrod).setToughness(0);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Axelrod Gunnarson");

        TestCards.mutableCard(blocker).setToughness(5);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Barbary Apes");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addAttackingAxelrod() {
        Permanent axelrod = addCreatureReady(player1, new AxelrodGunnarson());
        axelrod.setAttacking(true);
        return axelrod;
    }

    private Permanent addBlocker(int power, int toughness) {
        Permanent blocker = addCreatureReady(player2, new BarbaryApes());
        Card blockerCard = TestCards.mutableCard(blocker);
        blockerCard.setPower(power);
        blockerCard.setToughness(toughness);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        return blocker;
    }

    private void passCombatDamage(Permanent blocker) {
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 5));
    }
}
