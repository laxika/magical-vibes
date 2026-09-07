package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GolgariRotFarm.class, Forest.class})
class GolgariRotFarmTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and returns a chosen land to its owner's hand")
    void entersTappedAndReturnsChosenLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new GolgariRotFarm()));

        harness.playLand(player1, 0);

        Permanent rotFarm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof GolgariRotFarm)
                .findFirst().orElseThrow();
        assertThat(rotFarm.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, forest.getId());

        harness.assertOnBattlefield(player1, "Golgari Rot Farm");
        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Can return itself when it is the only land")
    void canReturnItself() {
        harness.setHand(player1, List.of(new GolgariRotFarm()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent rotFarm = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactly(rotFarm.getId());
        harness.handlePermanentChosen(player1, rotFarm.getId());

        harness.assertNotOnBattlefield(player1, "Golgari Rot Farm");
        harness.assertInHand(player1, "Golgari Rot Farm");
    }

    @Test
    @DisplayName("Tapping adds one black and one green mana")
    void manaAbilityAddsBlackAndGreen() {
        harness.addToBattlefield(player1, new GolgariRotFarm());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }
}
