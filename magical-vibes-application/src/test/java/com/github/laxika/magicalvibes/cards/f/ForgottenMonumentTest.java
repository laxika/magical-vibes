package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({ForgottenMonument.class})
class ForgottenMonumentTest extends BaseCardTest {

    @Test
    @DisplayName("Forgotten Monument adds one colorless mana")
    void addsColorlessMana() {
        harness.addToBattlefield(player1, new ForgottenMonument());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Other Caves you control gain the life-for-any-color mana ability")
    void grantsAbilityToOtherCavesYouControl() {
        harness.addToBattlefield(player1, new ForgottenMonument());
        Permanent cave = harness.addToBattlefieldAndReturn(player1, caveCard());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(cave.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The grant excludes Forgotten Monument and lands without the Cave subtype")
    void excludesSourceAndNonCaves() {
        harness.addToBattlefield(player1, new ForgottenMonument());
        harness.addToBattlefield(player1, plainLandCard());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The grant does not affect an opponent's Cave")
    void excludesOpponentsCaves() {
        harness.addToBattlefield(player1, new ForgottenMonument());
        harness.addToBattlefield(player2, caveCard());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Two Monuments grant each other mana abilities that produce any color")
    void monumentsGrantEachOtherAnyColorMana(ManaColor color) {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ForgottenMonument());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ForgottenMonument());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, color.name());
        harness.activateAbility(player1, 1, 1, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(2);
        harness.assertLife(player1, 18);
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Monument loses the granted ability when the other Monument leaves")
    void grantEndsWhenSourceLeaves() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ForgottenMonument());
        harness.addToBattlefield(player1, new ForgottenMonument());

        gd.playerBattlefields.get(player1.getId()).remove(source);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Insufficient life prevents paying for a granted mana ability without tapping")
    void cannotPayLifeCostAtZeroLife() {
        harness.addToBattlefield(player1, new ForgottenMonument());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new ForgottenMonument());
        harness.setLife(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(other.isTapped()).isFalse();
        harness.assertLife(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    private static Card caveCard() {
        Card card = new Card();
        card.setName("Test Cave");
        card.setType(CardType.LAND);
        card.setSubtypes(List.of(CardSubtype.CAVE));
        return card;
    }

    private static Card plainLandCard() {
        Card card = new Card();
        card.setName("Test Land");
        card.setType(CardType.LAND);
        return card;
    }
}
