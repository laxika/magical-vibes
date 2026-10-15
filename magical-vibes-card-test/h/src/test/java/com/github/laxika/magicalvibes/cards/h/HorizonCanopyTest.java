package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(HorizonCanopy.class)
class HorizonCanopyTest extends BaseCardTest {

    @Test
    @DisplayName("{T}, Pay 1 life: Add {G} or {W} produces green mana and costs 1 life")
    void tapsForGreen() {
        addReadyCanopy(player1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("GREEN", "WHITE");
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("{T}, Pay 1 life: Add {G} or {W} produces white mana and costs 1 life")
    void tapsForWhite() {
        addReadyCanopy(player1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("GREEN", "WHITE");
        harness.handleListChoice(player1, ManaColor.WHITE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("{1}, {T}, Sacrifice this land: Draw a card draws and sacrifices Horizon Canopy")
    void sacrificesToDraw() {
        addReadyCanopy(player1);
        harness.setLibrary(player1, List.of(new HorizonCanopy()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.assertNotOnBattlefield(player1, "Horizon Canopy");
        harness.assertInGraveyard(player1, "Horizon Canopy");
    }

    @Test
    void sacrificeIsPaidBeforeDrawResolves() {
        addReadyCanopy(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new HorizonCanopy()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Horizon Canopy");
        harness.assertInGraveyard(player1, "Horizon Canopy");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Horizon Canopy");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 20);
    }

    @Test
    void cannotSacrificeWithoutPayingMana() {
        addReadyCanopy(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Horizon Canopy");
        harness.assertNotInGraveyard(player1, "Horizon Canopy");
        assertThat(findPermanent(player1, "Horizon Canopy").isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void lifeIsPaidBeforeManaColorIsChosen() {
        addReadyCanopy(player1);
        harness.setLife(player1, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertLife(player1, 1);
        assertThat(findPermanent(player1, "Horizon Canopy").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();

        harness.handleListChoice(player1, ManaColor.WHITE.name());

        harness.assertLife(player1, 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    void tappedCanopyCannotActivateEitherAbility() {
        addReadyCanopy(player1);
        findPermanent(player1, "Horizon Canopy").tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Horizon Canopy");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void newlyEnteredLandCanTapForMana() {
        addReadyCanopy(player1);
        findPermanent(player1, "Horizon Canopy").setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        harness.assertLife(player1, 19);
        assertThat(findPermanent(player1, "Horizon Canopy").isTapped()).isTrue();
    }

    private void addReadyCanopy(Player player) {
        var permanent = harness.addToBattlefieldAndReturn(player, new HorizonCanopy());
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
