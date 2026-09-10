package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GroundSeal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VileMutilator.class, GrizzlyBears.class, GroundSeal.class, FountainOfYouth.class})
class VileMutilatorTest extends BaseCardTest {

    @Test
    @DisplayName("Can sacrifice a creature as an additional cost")
    void sacrificesCreatureAsAdditionalCost() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareCast();

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can sacrifice an enchantment as an additional cost")
    void sacrificesEnchantmentAsAdditionalCost() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GroundSeal());
        prepareCast();

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());

        harness.assertInGraveyard(player1, "Ground Seal");
    }

    @Test
    @DisplayName("Cannot sacrifice a noncreature nonenchantment permanent as an additional cost")
    void rejectsOtherPermanentAsAdditionalCost() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or enchantment");
    }

    @Test
    @DisplayName("Each opponent sacrifices a nontoken enchantment, then a nontoken creature")
    void sacrificesNontokenEnchantmentThenNontokenCreature() {
        Permanent cost = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GroundSeal());
        Permanent enchantmentToken = harness.addToBattlefieldAndReturn(player2, tokenCopy(new GroundSeal()));
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent creatureToken = harness.addToBattlefieldAndReturn(player2, tokenCopy(new GrizzlyBears()));
        prepareCast();

        harness.castSorceryWithSacrifice(player1, 0, cost.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(enchantmentToken, creatureToken);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Ground Seal", "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(enchantment, creature);
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new VileMutilator()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    private com.github.laxika.magicalvibes.model.Card tokenCopy(com.github.laxika.magicalvibes.model.Card card) {
        card.setToken(true);
        return card;
    }
}
