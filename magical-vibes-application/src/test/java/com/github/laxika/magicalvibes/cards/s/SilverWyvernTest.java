package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SilverWyvernTest extends BaseCardTest {

    @Test
    @DisplayName("Redirects a spell targeting only Silver Wyvern to another creature")
    void redirectsSpellTargetingOnlySilverWyvern() {
        SilverWyvern wyvern = new SilverWyvern();
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, wyvern);
        harness.addToBattlefield(player1, bears);
        UUID wyvernPermId = harness.getPermanentId(player1, "Silver Wyvern");
        UUID bearsPermId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, wyvernPermId);
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, shock.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bearsPermId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Silver Wyvern");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a spell that targets another creature")
    void cannotTargetSpellThatTargetsAnotherCreature() {
        SilverWyvern wyvern = new SilverWyvern();
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, wyvern);
        harness.addToBattlefield(player1, bears);
        UUID bearsPermId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, bearsPermId);
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.BLUE, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
