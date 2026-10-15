package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KashiTribeWarriors;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoratamiCloudskater.class, Island.class, Plains.class, Forest.class, KashiTribeWarriors.class})
class SoratamiCloudskaterTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a land as cost, then draws and discards on resolution")
    void returnsLandThenLoots() {
        harness.addToBattlefield(player1, new SoratamiCloudskater());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new KashiTribeWarriors()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, "Soratami Cloudskater"), null, null);

        harness.assertInHand(player1, "Island");
        harness.assertNotOnBattlefield(player1, "Island");

        harness.passBothPriorities();

        // Hand is [Kashi-Tribe Warriors, Island, Forest] after the draw
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Kashi-Tribe Warriors");
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
    @DisplayName("Cannot use an opponent's land to pay the return cost")
    void cannotActivateWithOnlyOpponentsLand() {
        harness.addToBattlefield(player1, new SoratamiCloudskater());
        harness.addToBattlefield(player2, new Island());
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

    @Test
    @DisplayName("Can discard the card just drawn with an initially empty hand")
    void canDiscardNewlyDrawnCard() {
        harness.addToBattlefield(player1, new SoratamiCloudskater());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, "Soratami Cloudskater"), null, null);

        harness.assertInHand(player1, "Island");
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertInHand(player1, "Forest");
        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Island");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick, returning a tapped land")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent cloudskater = harness.addToBattlefieldAndReturn(player1, new SoratamiCloudskater());
        cloudskater.tap();
        cloudskater.setSummoningSick(true);
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.tap();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, "Soratami Cloudskater"), null, null);

        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertInHand(player1, "Island");
        assertThat(cloudskater.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Island");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot activate with less than two mana, even with a land available")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new SoratamiCloudskater());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Soratami Cloudskater"), null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Island");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private int battlefieldIndex(Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).indexOf(findPermanent(owner, name));
    }
}
