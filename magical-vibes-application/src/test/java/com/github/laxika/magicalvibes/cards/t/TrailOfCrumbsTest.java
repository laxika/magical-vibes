package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrailOfCrumbs.class, GrizzlyBears.class, Shock.class})
class TrailOfCrumbsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a Food token")
    void entersWithFoodToken() {
        castTrailOfCrumbs();

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing a Food offers a permanent from the top two after paying {1}")
    void sacrificingFoodPaysAndFindsPermanent() {
        Card permanent = new GrizzlyBears();
        Card instant = new Shock();
        harness.setLibrary(player1, List.of(permanent, instant));
        castTrailOfCrumbs();

        Permanent food = findPermanent(player1, "Food");
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(food);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, foodIndex, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(permanent);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(permanent);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(instant);
    }

    @Test
    @DisplayName("Declining the payment does not look at the library")
    void decliningPaymentDoesNothing() {
        Card permanent = new GrizzlyBears();
        Card instant = new Shock();
        harness.setLibrary(player1, List.of(permanent, instant));
        castTrailOfCrumbs();

        Permanent food = findPermanent(player1, "Food");
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(food);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, foodIndex, null, null);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(permanent, instant);
    }

    private void castTrailOfCrumbs() {
        harness.setHand(player1, List.of(new TrailOfCrumbs()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
