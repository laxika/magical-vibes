package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SquirrelNestTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land's granted ability creates a 1/1 green Squirrel token")
    void grantedAbilityCreatesSquirrelToken() {
        Permanent forest = setUpEnchantedForest();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .singleElement()
                .satisfies(squirrel -> {
                    assertThat(squirrel.getCard().getName()).isEqualTo("Squirrel");
                    assertThat(squirrel.getCard().getPower()).isEqualTo(1);
                    assertThat(squirrel.getCard().getToughness()).isEqualTo(1);
                    assertThat(squirrel.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(squirrel.getCard().getSubtypes()).containsExactly(CardSubtype.SQUIRREL);
                });
    }

    @Test
    @DisplayName("A tapped enchanted land cannot activate the granted ability")
    void tappedLandCannotActivate() {
        Permanent forest = setUpEnchantedForest();
        forest.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Squirrel Nest can enchant only a land")
    void cannotEnchantCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SquirrelNest()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent setUpEnchantedForest() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SquirrelNest());
        aura.setAttachedTo(forest.getId());
        return forest;
    }
}
