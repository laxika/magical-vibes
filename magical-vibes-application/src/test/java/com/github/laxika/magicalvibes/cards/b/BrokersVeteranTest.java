package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrokersVeteran.class, GrizzlyBears.class, Murder.class, Shock.class})
class BrokersVeteranTest extends BaseCardTest {

    @Test
    @DisplayName("When Brokers Veteran dies, it puts a shield counter on a creature you control")
    void putsShieldCounterOnTargetCreatureYouControl() {
        Permanent veteran = addCreatureReady(player1, new BrokersVeteran());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        destroyWithMurder(player2, veteran.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
    }

    @Test
    @DisplayName("The shield counter from the death trigger prevents one damage event")
    void shieldCounterPreventsDamage() {
        Permanent veteran = addCreatureReady(player1, new BrokersVeteran());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        destroyWithMurder(player2, veteran.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
        assertThat(bears.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(bears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The death trigger cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent veteran = addCreatureReady(player1, new BrokersVeteran());
        addCreatureReady(player2, new GrizzlyBears());

        destroyWithMurder(player2, veteran.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void destroyWithMurder(Player caster, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Murder()));
        harness.addMana(caster, ManaColor.BLACK, 3);

        gs.playCard(gd, caster, 0, 0, targetId, null);
        harness.passBothPriorities();
    }
}
