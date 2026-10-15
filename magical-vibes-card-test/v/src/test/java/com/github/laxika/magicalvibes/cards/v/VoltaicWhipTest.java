package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VoltaicWhip.class, GrizzlyBears.class})
class VoltaicWhipTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+0")
    void equippedCreatureGetsPowerBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent whip = addCreatureReady(player1, new VoltaicWhip());
        whip.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking alone draws a card and costs 1 life")
    void attackingAloneDrawsAndLosesLife() {
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.setLife(player1, 20);

        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent whip = addCreatureReady(player1, new VoltaicWhip());
        whip.setAttachedTo(creature.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Attacking with another creature does not trigger the draw and life loss")
    void attackingWithAnotherCreatureDoesNotTrigger() {
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.setLife(player1, 20);

        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent whip = addCreatureReady(player1, new VoltaicWhip());
        whip.setAttachedTo(creature.getId());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Equip {2} attaches Voltaic Whip to a creature you control")
    void equipAttachesToCreatureYouControl() {
        Permanent whip = addCreatureReady(player1, new VoltaicWhip());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(whip.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("The creature's controller draws and loses life even when an opponent controls the Whip")
    void opponentControlledWhipGrantsAbilityToCreatureController() {
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent whip = addCreatureReady(player2, new VoltaicWhip());
        whip.setAttachedTo(creature.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The attack trigger still resolves after the Whip leaves the battlefield")
    void attackTriggerSurvivesEquipmentLeavingBattlefield() {
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.setLife(player1, 20);

        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent whip = addCreatureReady(player1, new VoltaicWhip());
        whip.setAttachedTo(creature.getId());

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(whip);
        gd.playerGraveyards.get(player1.getId()).add(whip.getCard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("An unequipped creature attacking alone does not draw a card or lose life")
    void unequippedCreatureAttackingAloneDoesNotTrigger() {
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.setLife(player1, 20);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new VoltaicWhip());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
    }
}
