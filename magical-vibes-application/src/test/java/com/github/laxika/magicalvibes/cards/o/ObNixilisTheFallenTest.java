package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
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

@CardUsed({ObNixilisTheFallen.class, Forest.class, IntoTheRoil.class})
class ObNixilisTheFallenTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall may make the targeted player lose 3 life and put three counters on Ob Nixilis")
    void landfallLosesLifeAndAddsCounters() {
        Permanent obNixilis = addObNixilis();
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(obNixilis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining the landfall choice causes no life loss or counters")
    void decliningLandfallDoesNothing() {
        Permanent obNixilis = addObNixilis();
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(obNixilis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's land does not trigger Ob Nixilis")
    void opponentLandDoesNotTrigger() {
        addObNixilis();
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Landfall can target its controller")
    void landfallCanTargetController() {
        Permanent obNixilis = addObNixilis();
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
        assertThat(obNixilis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("A land entering without being played also triggers landfall")
    void landEnteringWithoutBeingPlayedTriggers() {
        Permanent obNixilis = addObNixilis();

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 17);
        assertThat(obNixilis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Landfall still causes life loss after Ob Nixilis leaves the battlefield")
    void landfallResolvesAfterSourceLeaves() {
        Permanent obNixilis = addObNixilis();
        harness.setHand(player1, List.of(new Forest(), new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.castInstant(player1, 0, obNixilis.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Ob Nixilis, the Fallen");
        harness.assertInHand(player1, "Ob Nixilis, the Fallen");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 17);
        assertThat(obNixilis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addObNixilis() {
        return harness.addToBattlefieldAndReturn(player1, new ObNixilisTheFallen());
    }
}
