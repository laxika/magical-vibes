package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GroundSeal;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Simplify")
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
        Permanent player1First = new Permanent(new GroundSeal());
        Permanent player1Second = new Permanent(new GroundSeal());
        Permanent player2First = new Permanent(new GroundSeal());
        Permanent player2Second = new Permanent(new GroundSeal());
        harness.getGameData().playerBattlefields.get(player1.getId()).addAll(List.of(player1First, player1Second));
        harness.getGameData().playerBattlefields.get(player2.getId()).addAll(List.of(player2First, player2Second));

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
    @DisplayName("Non-enchantments are not sacrificed")
    void nonEnchantmentsAreNotSacrificed() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        castSimplify();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
    }

    private void castSimplify() {
        harness.setHand(player1, List.of(new Simplify()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
