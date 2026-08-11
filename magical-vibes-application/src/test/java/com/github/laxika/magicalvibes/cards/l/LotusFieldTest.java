package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LotusFieldTest extends BaseCardTest {

    @Test
    @DisplayName("Entering sacrifices two lands of the controller's choice")
    void enterSacrificesTwoLands() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new LotusField()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId(), island.getId()));

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player1, "Lotus Field");
        assertThat(findPermanent(player1, "Lotus Field").isTapped()).isTrue();
    }

    @Test
    @DisplayName("With no other lands, Lotus Field sacrifices itself")
    void sacrificesItselfWhenItIsTheOnlyLand() {
        harness.setHand(player1, List.of(new LotusField()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lotus Field");
        harness.assertInGraveyard(player1, "Lotus Field");
    }

    @Test
    @DisplayName("Tapping Lotus Field adds three mana of the chosen color")
    void manaAbilityAddsThreeManaOfChosenColor() {
        harness.addToBattlefield(player1, new LotusField());

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "GREEN");

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }
}
