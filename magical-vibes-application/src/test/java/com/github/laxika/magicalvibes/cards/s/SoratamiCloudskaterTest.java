package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoratamiCloudskaterTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a land as cost, then draws and discards on resolution")
    void returnsLandThenLoots() {
        harness.addToBattlefield(player1, new SoratamiCloudskater());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, "Soratami Cloudskater"), null, null);

        harness.assertInHand(player1, "Island");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Island"));

        harness.passBothPriorities();

        // Hand is [Grizzly Bears, Island, Forest] after the draw
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot activate without a land to return")
    void cannotActivateWithoutLand() {
        harness.addToBattlefield(player1, new SoratamiCloudskater());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Soratami Cloudskater"), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Chooses which land to return when several are available")
    void choosesLandWhenSeveralAvailable() {
        harness.addToBattlefield(player1, new SoratamiCloudskater());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Plains());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        Permanent plains = findPermanent(player1, "Plains");

        harness.activateAbility(player1, battlefieldIndex(player1, "Soratami Cloudskater"), null, null);

        assertThat(gd.stack).isEmpty();

        harness.handlePermanentChosen(player1, plains.getId());

        assertThat(gd.stack).hasSize(1);
        harness.assertInHand(player1, "Plains");
        harness.assertOnBattlefield(player1, "Island");
    }

    private int battlefieldIndex(Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).indexOf(findPermanent(owner, name));
    }
}
