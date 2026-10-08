package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.FearOfFalling;
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

@CardUsed({VileMutilator.class, GrizzlyBears.class, GroundSeal.class, FountainOfYouth.class, FearOfFalling.class})
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

    @Test
    void canSacrificeTokenAsAdditionalCost() {
        Permanent cost = harness.addToBattlefieldAndReturn(player1, tokenCopy(new FearOfFalling()));
        prepareCast();

        harness.castSorceryWithSacrifice(player1, 0, cost.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cost);
        harness.assertOnBattlefield(player1, "Vile Mutilator");
    }

    @Test
    void cannotCastWithoutPayingSacrificeCost() {
        prepareCast();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Vile Mutilator");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotSacrificeOpponentsPermanentAsAdditionalCost() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new VileMutilator());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        harness.assertInHand(player1, "Vile Mutilator");
    }

    @Test
    void sacrificesCreatureEvenWhenOpponentHasNoEnchantment() {
        harness.addToBattlefield(player2, new VileMutilator());

        harness.enterBattlefieldAndReturn(player1, new VileMutilator());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Vile Mutilator");
        harness.assertOnBattlefield(player1, "Vile Mutilator");
    }

    @Test
    void doesNotSacrificeTokensWhenOpponentHasOnlyTokens() {
        Permanent enchantmentCreature = harness.addToBattlefieldAndReturn(player2, tokenCopy(new FearOfFalling()));
        Permanent creature = harness.addToBattlefieldAndReturn(player2, tokenCopy(new VileMutilator()));

        harness.enterBattlefieldAndReturn(player1, new VileMutilator());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(enchantmentCreature, creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void enchantmentCreatureSacrificedFirstCannotAlsoSatisfyCreatureSacrifice() {
        Permanent enchantmentCreature = harness.addToBattlefieldAndReturn(player2, new FearOfFalling());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new VileMutilator());
        Permanent controllerEnchantment = harness.addToBattlefieldAndReturn(player1, new FearOfFalling());

        harness.enterBattlefieldAndReturn(player1, new VileMutilator());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enchantmentCreature, creature);
        harness.assertInGraveyard(player2, "Fear of Falling");
        harness.assertInGraveyard(player2, "Vile Mutilator");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(controllerEnchantment);
        harness.assertOnBattlefield(player1, "Vile Mutilator");
    }

    @Test
    void opponentChoosesEnchantmentThenChoosesFromRemainingCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new FearOfFalling());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new FearOfFalling());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new VileMutilator());

        harness.enterBattlefieldAndReturn(player1, new VileMutilator());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(first, second, creature);
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId()));

        harness.assertInGraveyard(player2, "Fear of Falling");
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(first, creature);
        harness.handleMultiplePermanentsChosen(player2, List.of(creature.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(first);
        harness.assertInGraveyard(player2, "Vile Mutilator");
        assertThat(gd.stack).isEmpty();
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
