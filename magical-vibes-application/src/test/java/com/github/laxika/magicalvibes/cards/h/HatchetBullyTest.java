package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()); // 2/2
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Prompts for the creature to receive the -1/-1 counter when the player controls multiple creatures")
    void promptsForCounterChoiceWhenMultipleCreatures() {
        addReadyBully(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
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
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()); // 2/2
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
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

    private Permanent addReadyBully(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new HatchetBully());
        perm.setSummoningSick(false);
        return perm;
    }
}
