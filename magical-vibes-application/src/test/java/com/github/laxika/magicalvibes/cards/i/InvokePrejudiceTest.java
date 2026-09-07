package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InvokePrejudice.class, GrizzlyBears.class, LlanowarElves.class, SuntailHawk.class})
class InvokePrejudiceTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a creature spell with no shared color when its caster cannot pay")
    void countersCreatureSpellWithNoSharedColorWhenCasterCannotPay() {
        harness.addToBattlefield(player1, new InvokePrejudice());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new SuntailHawk()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("Does not trigger for a creature spell sharing a color with a creature you control")
    void doesNotTriggerForCreatureSpellSharingColor() {
        harness.addToBattlefield(player1, new InvokePrejudice());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new LlanowarElves()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Lets a creature spell resolve when its caster pays its mana value")
    void letsCreatureSpellResolveWhenCasterPaysManaValue() {
        harness.addToBattlefield(player1, new InvokePrejudice());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new SuntailHawk()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Suntail Hawk");
    }
}
