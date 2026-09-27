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

        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isZero();
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
}
