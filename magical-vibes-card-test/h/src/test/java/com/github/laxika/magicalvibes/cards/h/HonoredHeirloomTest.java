package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.t.TravelingMinister;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({HonoredHeirloom.class, TravelingMinister.class})
class HonoredHeirloomTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for any color adds the chosen mana")
    void tapsForAnyColor() {
        harness.addToBattlefield(player1, new HonoredHeirloom());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Exiles a target card from a graveyard")
    void exilesTargetCardFromGraveyard() {
        Card target = new TravelingMinister();
        harness.addToBattlefield(player1, new HonoredHeirloom());
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLACK", "RED", "GREEN"})
    void manaAbilityResolvesImmediatelyAndTapsSource(ManaColor color) {
        Permanent heirloom = harness.addToBattlefieldAndReturn(player1, new HonoredHeirloom());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(heirloom.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exilesOnlyChosenNoncreatureFromOwnGraveyardAndPaysCosts() {
        Card target = new HonoredHeirloom();
        Card other = new HonoredHeirloom();
        Permanent heirloom = harness.addToBattlefieldAndReturn(player1, new HonoredHeirloom());
        harness.setGraveyard(player1, List.of(target, other));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(target.getId()));

        assertThat(heirloom.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target, other);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(target);
    }

    @Test
    void cannotExileWithoutTwoMana() {
        Card target = new HonoredHeirloom();
        Permanent heirloom = harness.addToBattlefieldAndReturn(player1, new HonoredHeirloom());
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(heirloom.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateExileWithNoTarget() {
        harness.addToBattlefield(player1, new HonoredHeirloom());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void targetLeavingGraveyardDoesNotExileAnotherCard() {
        Card target = new HonoredHeirloom();
        Card other = new HonoredHeirloom();
        harness.addToBattlefield(player1, new HonoredHeirloom());
        harness.setGraveyard(player2, List.of(target, other));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(target.getId()));
        harness.setGraveyard(player2, List.of(other));

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(other);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
