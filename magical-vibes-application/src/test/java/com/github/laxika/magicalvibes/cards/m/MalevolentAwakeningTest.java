package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MalevolentAwakeningTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature returns a target creature card from the graveyard to hand")
    void sacrificesCreatureAndReturnsTargetCreatureToHand() {
        harness.addToBattlefield(player1, new MalevolentAwakening());
        addCreatureReady(player1, new GrizzlyBears());
        Card target = new LlanowarElves();
        harness.setGraveyard(player1, List.of(target));
        addActivationMana();

        harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Llanowar Elves");
        harness.assertInHand(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot target a noncreature card in the graveyard")
    void cannotTargetNoncreatureCard() {
        harness.addToBattlefield(player1, new MalevolentAwakening());
        addCreatureReady(player1, new GrizzlyBears());
        Card target = new Shock();
        harness.setGraveyard(player1, List.of(target));
        addActivationMana();

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
