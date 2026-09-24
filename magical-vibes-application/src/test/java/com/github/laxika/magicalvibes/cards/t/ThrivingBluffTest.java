package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ThrivingBluff.class)
class ThrivingBluffTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and offers every color except red")
    void entersTappedAndChoosesNonRedColor() {
        harness.setHand(player1, List.of(new ThrivingBluff()));

        harness.playLand(player1, 0);

        Permanent bluff = findPermanent(player1, "Thriving Bluff");
        assertThat(bluff.isTapped()).isTrue();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLUE", "BLACK", "GREEN");
        assertThat(choice.options()).doesNotContain("RED");

        harness.handleListChoice(player1, "BLUE");

        assertThat(bluff.getChosenColor()).isEqualTo(CardColor.BLUE);
    }

    @Test
    @DisplayName("The first mana ability adds red mana")
    void firstAbilityAddsRedMana() {
        Permanent bluff = addReadyBluff();
        bluff.setChosenColor(CardColor.BLUE);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(bluff.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The second mana ability adds mana of the chosen color")
    void secondAbilityAddsChosenColorMana() {
        Permanent bluff = addReadyBluff();
        bluff.setChosenColor(CardColor.GREEN);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(bluff.isTapped()).isTrue();
    }

    private Permanent addReadyBluff() {
        Permanent bluff = harness.addToBattlefieldAndReturn(player1, new ThrivingBluff());
        bluff.setSummoningSick(false);
        return bluff;
    }
}
