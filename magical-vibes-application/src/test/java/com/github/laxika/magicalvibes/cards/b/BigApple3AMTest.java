package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(BigApple3AM.class)
class BigApple3AMTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and stores the chosen color")
    void entersTappedAndStoresChosenColor() {
        harness.setHand(player1, List.of(new BigApple3AM()));

        harness.playLand(player1, 0);

        Permanent land = findPermanent(player1, "Big Apple, 3 a.m.");
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(land.getChosenColor()).isEqualTo(CardColor.BLUE);
    }

    @Test
    @DisplayName("Tapping adds one mana of the chosen color")
    void tappingAddsChosenColorMana() {
        Permanent land = addReadyLand(CardColor.GREEN);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creates one black Rat token for the opponent")
    void createsRatForEachOpponent() {
        addReadyLand(CardColor.BLACK);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        List<Permanent> rats = findPermanents(player1, "Rat");
        assertThat(rats).hasSize(1);
        assertThat(rats.getFirst().getCard().getColors()).containsExactly(CardColor.BLACK);
        assertThat(rats.getFirst().getCard().getSubtypes()).contains(CardSubtype.RAT);
        assertThat(rats.getFirst().getEffectivePower()).isEqualTo(1);
        assertThat(rats.getFirst().getEffectiveToughness()).isEqualTo(1);
    }

    @ParameterizedTest
    @EnumSource(value = CardColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("The color chosen on entry determines the mana produced after untapping")
    void producesEachChosenColorAfterEntering(CardColor color) {
        harness.setHand(player1, List.of(new BigApple3AM()));
        harness.playLand(player1, 0);
        harness.handleListChoice(player1, color.name());
        assertThat(gd.stack).isEmpty();

        harness.performUntapStep(player1);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.valueOf(color.name())))
                .isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Big Apple, 3 a.m.").isTapped()).isTrue();
    }

    @Test
    @DisplayName("The Rat ability pays five mana and taps immediately but creates tokens on resolution")
    void ratAbilityPaysCostsBeforeResolving() {
        Permanent land = addReadyLand(CardColor.BLUE);
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player1, "Rat")).isEmpty();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Rat")).hasSize(1);
        assertThat(findPermanents(player2, "Rat")).isEmpty();
        assertThat(findPermanent(player1, "Rat").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Four mana cannot pay for the Rat ability")
    void ratAbilityRequiresFiveMana() {
        Permanent land = addReadyLand(CardColor.BLACK);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(land.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Rat")).isEmpty();
    }

    @Test
    @DisplayName("Neither tap ability can be activated while the land is tapped")
    void tappedLandCannotActivateEitherAbility() {
        harness.setHand(player1, List.of(new BigApple3AM()));
        harness.playLand(player1, 0);
        harness.handleListChoice(player1, "BLACK");
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(5);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Rat")).isEmpty();
    }

    private Permanent addReadyLand(CardColor chosenColor) {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new BigApple3AM());
        land.setSummoningSick(false);
        land.setChosenColor(chosenColor);
        return land;
    }
}
