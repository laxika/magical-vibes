package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RakdosCarnarium.class, Mountain.class})
class RakdosCarnariumTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and returns a chosen land to its owner's hand")
    void entersTappedAndReturnsChosenLand() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new RakdosCarnarium()));

        harness.playLand(player1, 0);

        Permanent carnarium = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof RakdosCarnarium)
                .findFirst().orElseThrow();
        assertThat(carnarium.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, mountain.getId());

        harness.assertOnBattlefield(player1, "Rakdos Carnarium");
        harness.assertInHand(player1, "Mountain");
        harness.assertNotOnBattlefield(player1, "Mountain");
    }

    @Test
    @DisplayName("Can return itself when it is the only land")
    void canReturnItself() {
        harness.setHand(player1, List.of(new RakdosCarnarium()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent carnarium = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactly(carnarium.getId());
        harness.handlePermanentChosen(player1, carnarium.getId());

        harness.assertNotOnBattlefield(player1, "Rakdos Carnarium");
        harness.assertInHand(player1, "Rakdos Carnarium");
    }

    @Test
    @DisplayName("Tapping adds one black and one red mana")
    void manaAbilityAddsBlackAndRed() {
        Permanent carnarium = harness.addToBattlefieldAndReturn(player1, new RakdosCarnarium());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(carnarium.isTapped()).isTrue();
    }
}
