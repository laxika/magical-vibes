package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TemurBanner.class, AlpineGrizzly.class})
class TemurBannerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Temur Banner adds green, blue, or red mana")
    void tappingAddsChosenMana() {
        Permanent banner = addReadyBanner();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(banner.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying green, blue, and red mana sacrifices Temur Banner and draws a card")
    void payingManaSacrificesAndDraws() {
        Permanent banner = addReadyBanner();
        harness.setLibrary(player1, List.of(new AlpineGrizzly()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(banner);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(banner.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof AlpineGrizzly);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"GREEN", "RED"})
    @DisplayName("A newly controlled noncreature banner can immediately produce either other color")
    void newlyControlledBannerProducesOtherColors(ManaColor chosenColor) {
        Permanent banner = harness.addToBattlefieldAndReturn(player1, new TemurBanner());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, chosenColor.name());

        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color))
                    .isEqualTo(color == chosenColor ? 1 : 0);
        }
        assertThat(banner.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Neither ability can be activated while the banner is tapped")
    void tappedBannerCannotActivate(int abilityIndex) {
        Permanent banner = addReadyBanner();
        banner.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(banner);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        for (ManaColor color : List.of(ManaColor.GREEN, ManaColor.BLUE, ManaColor.RED)) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        }
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"GREEN", "BLUE", "RED"})
    @DisplayName("The draw ability requires each specified color and cannot use colorless mana instead")
    void drawRequiresAllThreeColors(ManaColor missingColor) {
        Permanent banner = addReadyBanner();
        for (ManaColor color : List.of(ManaColor.GREEN, ManaColor.BLUE, ManaColor.RED)) {
            if (color != missingColor) {
                harness.addMana(player1, color, 1);
            }
        }
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(banner.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(banner);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        for (ManaColor color : List.of(ManaColor.GREEN, ManaColor.BLUE, ManaColor.RED)) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color))
                    .isEqualTo(color == missingColor ? 0 : 1);
        }
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("A newly controlled banner is sacrificed immediately but draws only on resolution")
    void drawUsesStackAfterPayingCosts() {
        Permanent banner = harness.addToBattlefieldAndReturn(player1, new TemurBanner());
        AlpineGrizzly drawnCard = new AlpineGrizzly();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(banner);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(banner.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1).contains(drawnCard);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyBanner() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new TemurBanner());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
