package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GroundSeal;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Simplify")
@CardUsed({Simplify.class, Forest.class, GroundSeal.class})
class SimplifyTest extends BaseCardTest {

    @Test
    @DisplayName("Each player sacrifices their only enchantment")
    void eachPlayerSacrificesTheirOnlyEnchantment() {
        harness.addToBattlefield(player1, new GroundSeal());
        harness.addToBattlefield(player2, new GroundSeal());

        castSimplify();

        harness.assertInGraveyard(player1, "Ground Seal");
        harness.assertInGraveyard(player2, "Ground Seal");
    }

    @Test
    @DisplayName("Each player chooses which enchantment to sacrifice")
    void eachPlayerChoosesEnchantmentToSacrifice() {
        Permanent player1First = harness.addToBattlefieldAndReturn(player1, new GroundSeal());
        Permanent player1Second = harness.addToBattlefieldAndReturn(player1, new GroundSeal());
        Permanent player2First = harness.addToBattlefieldAndReturn(player2, new GroundSeal());
        Permanent player2Second = harness.addToBattlefieldAndReturn(player2, new GroundSeal());

        castSimplify();

        GameData gd = harness.getGameData();
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(player1First.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(player2Second.getId()));

        assertThat(countPermanents(player1, "Ground Seal")).isEqualTo(1);
        assertThat(countPermanents(player2, "Ground Seal")).isEqualTo(1);
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Only enchantments are offered when other permanents are present")
    void onlyEnchantmentsAreOfferedWhenOtherPermanentsArePresent() {
        Permanent player1FirstEnchantment = harness.addToBattlefieldAndReturn(player1, new GroundSeal());
        harness.addToBattlefield(player1, new Forest());
        Permanent player1SecondEnchantment = harness.addToBattlefieldAndReturn(player1, new GroundSeal());
        Permanent player2FirstEnchantment = harness.addToBattlefieldAndReturn(player2, new GroundSeal());
        harness.addToBattlefield(player2, new Forest());
        Permanent player2SecondEnchantment = harness.addToBattlefieldAndReturn(player2, new GroundSeal());

        castSimplify();

        PendingInteraction.MultiPermanentChoice player1Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player1Choice).isNotNull();
        assertThat(player1Choice.validIds())
                .containsExactly(player1FirstEnchantment.getId(), player1SecondEnchantment.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(player1FirstEnchantment.getId()));

        PendingInteraction.MultiPermanentChoice player2Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player2Choice).isNotNull();
        assertThat(player2Choice.validIds())
                .containsExactly(player2FirstEnchantment.getId(), player2SecondEnchantment.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(player2SecondEnchantment.getId()));

        assertThat(countPermanents(player1, "Ground Seal")).isEqualTo(1);
        assertThat(countPermanents(player2, "Ground Seal")).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Non-enchantments are not sacrificed")
    void nonEnchantmentsAreNotSacrificed() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        castSimplify();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
    }

    private void castSimplify() {
        harness.castFromHand(player1, new Simplify(), "{G}");
        harness.passBothPriorities();
    }
}
