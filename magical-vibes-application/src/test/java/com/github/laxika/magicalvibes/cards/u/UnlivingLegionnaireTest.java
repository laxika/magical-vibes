package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnlivingLegionnaire.class, GrizzlyBears.class, Shock.class})
class UnlivingLegionnaireTest extends BaseCardTest {

    @Test
    @DisplayName("Power-up returns a creature card and puts two counters on this creature")
    void powerUpReturnsCreatureAndPutsCountersOnSource() {
        Permanent legionnaire = harness.enterBattlefieldAndReturn(player1, new UnlivingLegionnaire());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(legionnaire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Power-up can resolve without choosing a creature card")
    void powerUpCanResolveWithoutTarget() {
        Permanent legionnaire = harness.enterBattlefieldAndReturn(player1, new UnlivingLegionnaire());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null, Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(legionnaire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Power-up cannot target a noncreature card")
    void powerUpCannotTargetNoncreatureCard() {
        addCreatureReady(player1, new UnlivingLegionnaire());
        Card noncreature = new Shock();
        harness.setGraveyard(player1, List.of(noncreature));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, noncreature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Power-up can be activated only once")
    void powerUpCanBeActivatedOnlyOnce() {
        Permanent legionnaire = addCreatureReady(player1, new UnlivingLegionnaire());
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, 0, null, null, Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(legionnaire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }
}
