package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(UnchartedHaven.class)
class UnchartedHavenTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and asks its controller to choose a color")
    void entersTappedAndChoosesColor() {
        harness.setHand(player1, List.of(new UnchartedHaven()));

        harness.playLand(player1, 0);

        Permanent haven = findPermanent(player1, "Uncharted Haven");
        assertThat(haven.isTapped()).isTrue();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLUE", "BLACK", "RED", "GREEN");

        harness.handleListChoice(player1, "BLUE");

        assertThat(haven.getChosenColor()).isEqualTo(CardColor.BLUE);
    }

    @Test
    @DisplayName("Tapping it adds one mana of its chosen color")
    void tappingAddsChosenColorMana() {
        Permanent haven = harness.addToBattlefieldAndReturn(player1, new UnchartedHaven());
        haven.setSummoningSick(false);
        haven.setChosenColor(CardColor.RED);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(haven.isTapped()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(CardColor.class)
    @DisplayName("Each color chosen on entry produces exactly one mana immediately")
    void producesEachChosenColor(CardColor color) {
        harness.setHand(player1, List.of(new UnchartedHaven()));
        harness.playLand(player1, 0);
        harness.handleListChoice(player1, color.name());
        harness.performUntapStep(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        for (ManaColor manaColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor))
                    .isEqualTo(manaColor.name().equals(color.name()) ? 1 : 0);
        }
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Uncharted Haven").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Separate Havens retain independent color choices")
    void separateHavensChooseIndependentColors() {
        Permanent first = harness.enterBattlefieldAndReturn(player1, new UnchartedHaven());
        harness.handleListChoice(player1, "WHITE");
        Permanent second = harness.enterBattlefieldAndReturn(player1, new UnchartedHaven());
        harness.handleListChoice(player1, "GREEN");
        harness.performUntapStep(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
