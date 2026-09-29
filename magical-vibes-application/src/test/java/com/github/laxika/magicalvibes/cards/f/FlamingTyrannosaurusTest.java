package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlamingTyrannosaurus.class, GrizzlyBears.class, Murder.class})
class FlamingTyrannosaurusTest extends BaseCardTest {

    @Test
    @DisplayName("Paradox deals 3 damage to any target and puts a +1/+1 counter on Flaming Tyrannosaurus")
    void paradoxDealsDamageAndAddsCounter() {
        Permanent tyrannosaurus = harness.addToBattlefieldAndReturn(player1, new FlamingTyrannosaurus());
        GrizzlyBears spell = new GrizzlyBears();
        gd.addToExile(player1.getId(), spell);
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castFromExile(player1, spell.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
        assertThat(tyrannosaurus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a spell from hand does not trigger Paradox")
    void handSpellDoesNotTriggerParadox() {
        Permanent tyrannosaurus = harness.addToBattlefieldAndReturn(player1, new FlamingTyrannosaurus());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(tyrannosaurus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Death trigger deals damage equal to Flaming Tyrannosaurus's power to each opponent")
    void deathTriggerDealsPowerDamageToEachOpponent() {
        Permanent tyrannosaurus = harness.addToBattlefieldAndReturn(player1, new FlamingTyrannosaurus());
        tyrannosaurus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castInstant(player2, 0, tyrannosaurus.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        harness.assertInGraveyard(player1, "Flaming Tyrannosaurus");
    }
}
