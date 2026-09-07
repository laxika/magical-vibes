package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AzoriusChancery.class, Island.class})
class AzoriusChanceryTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and returns a chosen land to its owner's hand")
    void entersTappedAndReturnsChosenLand() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new AzoriusChancery()));

        harness.playLand(player1, 0);

        Permanent chancery = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof AzoriusChancery)
                .findFirst().orElseThrow();
        assertThat(chancery.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, island.getId());

        harness.assertOnBattlefield(player1, "Azorius Chancery");
        harness.assertInHand(player1, "Island");
        harness.assertNotOnBattlefield(player1, "Island");
    }

    @Test
    @DisplayName("Can return itself when it is the only land")
    void canReturnItself() {
        harness.setHand(player1, List.of(new AzoriusChancery()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent chancery = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactly(chancery.getId());
        harness.handlePermanentChosen(player1, chancery.getId());

        harness.assertNotOnBattlefield(player1, "Azorius Chancery");
        harness.assertInHand(player1, "Azorius Chancery");
    }

    @Test
    @DisplayName("Tapping adds one white and one blue mana")
    void manaAbilityAddsWhiteAndBlue() {
        Permanent chancery = harness.addToBattlefieldAndReturn(player1, new AzoriusChancery());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(chancery.isTapped()).isTrue();
    }
}
