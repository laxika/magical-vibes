package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.n.NettleSentinel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HatchetBully.class, NettleSentinel.class})
class HatchetBullyTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to a player and auto-puts a -1/-1 counter on itself when it is the only creature")
    void dealsDamageToPlayerAndPutsCounterOnSelf() {
        Permanent bully = addReadyBully(player1);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(bully.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals 2 damage to a target creature, destroying it")
    void dealsDamageToTargetCreature() {
        addReadyBully(player1);
        Permanent sentinel = harness.addToBattlefieldAndReturn(player2, new NettleSentinel()); // 2/2
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, sentinel.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Nettle Sentinel");
    }

    @Test
    @DisplayName("Prompts for the creature to receive the -1/-1 counter when the player controls multiple creatures")
    void promptsForCounterChoiceWhenMultipleCreatures() {
        addReadyBully(player1);
        harness.addToBattlefield(player1, new NettleSentinel());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("Puts the -1/-1 counter on the chosen creature when prompted, then deals damage")
    void putsCounterOnChosenCreature() {
        addReadyBully(player1);
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new NettleSentinel()); // 2/2
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, sentinel.getId());
        harness.passBothPriorities();

        assertThat(sentinel.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cannot activate without paying the {2}{R} cost")
    void requiresMana() {
        addReadyBully(player1);
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2); // missing the red

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate again while tapped")
    void requiresSourceToBeUntapped() {
        Permanent bully = addReadyBully(player1);
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(bully.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target itself with the damage")
    void canTargetItself() {
        Permanent bully = addReadyBully(player1);
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, bully.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hatchet Bully");
    }

    private Permanent addReadyBully(Player player) {
        return addCreatureReady(player, new HatchetBully());
    }
}
