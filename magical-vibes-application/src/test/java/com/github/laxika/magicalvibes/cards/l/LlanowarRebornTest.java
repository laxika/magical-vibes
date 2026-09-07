package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LlanowarReborn.class, GrizzlyBears.class})
class LlanowarRebornTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped with a +1/+1 counter")
    void entersTappedWithCounter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LlanowarReborn()));

        harness.playLand(player1, 0);

        Permanent reborn = findPermanent(player1, "Llanowar Reborn");
        assertThat(reborn.isTapped()).isTrue();
        assertThat(reborn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping adds one green mana")
    void tapsForGreenMana() {
        Permanent reborn = harness.addToBattlefieldAndReturn(player1, new LlanowarReborn());
        reborn.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(reborn.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Its controller may move its counter onto a creature that enters")
    void controllerMayMoveCounterOntoEnteringCreature() {
        Permanent reborn = addRebornWithCounter();
        castCreature(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(reborn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining graft leaves the counter on the land")
    void decliningGraftLeavesCounterOnLand() {
        Permanent reborn = addRebornWithCounter();
        castCreature(player1);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(reborn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The land's controller chooses for an opponent's creature")
    void controllerChoosesForOpponentsCreature() {
        Permanent reborn = addRebornWithCounter();
        castCreature(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(reborn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player2, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addRebornWithCounter() {
        Permanent reborn = harness.addToBattlefieldAndReturn(player1, new LlanowarReborn());
        reborn.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        return reborn;
    }

    private void castCreature(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player, List.of(new GrizzlyBears()));
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
