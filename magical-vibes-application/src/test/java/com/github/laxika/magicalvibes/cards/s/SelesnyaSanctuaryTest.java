package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({SelesnyaSanctuary.class, Forest.class})
class SelesnyaSanctuaryTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and returns a chosen land to its owner's hand")
    void entersTappedAndReturnsChosenLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new SelesnyaSanctuary()));

        harness.playLand(player1, 0);

        Permanent sanctuary = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SelesnyaSanctuary)
                .findFirst().orElseThrow();
        assertThat(sanctuary.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, forest.getId());

        harness.assertOnBattlefield(player1, "Selesnya Sanctuary");
        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Can return itself when it is the only land")
    void canReturnItself() {
        harness.setHand(player1, List.of(new SelesnyaSanctuary()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent sanctuary = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactly(sanctuary.getId());
        harness.handlePermanentChosen(player1, sanctuary.getId());

        harness.assertNotOnBattlefield(player1, "Selesnya Sanctuary");
        harness.assertInHand(player1, "Selesnya Sanctuary");
    }

    @Test
    @DisplayName("Tapping adds one green and one white mana")
    void manaAbilityAddsGreenAndWhite() {
        Permanent sanctuary = harness.addToBattlefieldAndReturn(player1, new SelesnyaSanctuary());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(sanctuary.isTapped()).isTrue();
    }
}
