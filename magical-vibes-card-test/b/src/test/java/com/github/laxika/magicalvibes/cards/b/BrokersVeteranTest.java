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
        harness.castAndResolveInstant(player2, 0, bears.getId());

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

    @Test
    @DisplayName("The shield counter prevents one destruction event but not a second")
    void shieldCounterPreventsOneDestruction() {
        Permanent veteran = addCreatureReady(player1, new BrokersVeteran());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        destroyWithMurder(player2, veteran.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        destroyWithMurder(player2, bears.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
        assertThat(bears.getCounterCount(CounterType.SHIELD)).isZero();

        destroyWithMurder(player2, bears.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A death trigger does not put a counter on a target destroyed in response")
    void targetDestroyedInResponseDoesNotReceiveCounter() {
        Permanent veteran = addCreatureReady(player1, new BrokersVeteran());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        destroyWithMurder(player2, veteran.getId());
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.stack).hasSize(1);
        destroyWithMurder(player2, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        assertThat(bears.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Lethal damage also triggers Brokers Veteran's shield counter ability")
    void lethalDamageTriggersShieldCounter() {
        Permanent veteran = addCreatureReady(player1, new BrokersVeteran());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, veteran.getId());

        harness.assertInGraveyard(player1, "Brokers Veteran");
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
    }

    private void destroyWithMurder(Player caster, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Murder()));
        harness.addMana(caster, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(caster, 0, targetId);
    }
}
