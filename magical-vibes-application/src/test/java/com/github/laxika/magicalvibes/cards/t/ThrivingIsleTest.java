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

@CardUsed(ThrivingIsle.class)
class ThrivingIsleTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and offers every color except blue")
    void entersTappedAndChoosesNonBlueColor() {
        harness.setHand(player1, List.of(new ThrivingIsle()));

        harness.playLand(player1, 0);

        Permanent isle = findPermanent(player1, "Thriving Isle");
        assertThat(isle.isTapped()).isTrue();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLACK", "RED", "GREEN");
        assertThat(choice.options()).doesNotContain("BLUE");

        harness.handleListChoice(player1, "GREEN");

        assertThat(isle.getChosenColor()).isEqualTo(CardColor.GREEN);
    }

    @Test
    @DisplayName("The first mana ability adds blue mana")
    void firstAbilityAddsBlueMana() {
        Permanent isle = addReadyIsle();
        isle.setChosenColor(CardColor.GREEN);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(isle.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The second mana ability adds mana of the chosen color")
    void secondAbilityAddsChosenColorMana() {
        Permanent isle = addReadyIsle();
        isle.setChosenColor(CardColor.RED);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(isle.isTapped()).isTrue();
    }

    private Permanent addReadyIsle() {
        Permanent isle = harness.addToBattlefieldAndReturn(player1, new ThrivingIsle());
        isle.setSummoningSick(false);
        return isle;
    }
}
