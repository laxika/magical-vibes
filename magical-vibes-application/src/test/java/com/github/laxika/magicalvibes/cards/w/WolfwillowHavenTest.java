package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WolfwillowHaven.class, Forest.class, GrizzlyBears.class})
class WolfwillowHavenTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping the enchanted land adds an additional green mana")
    void enchantedLandAddsGreenMana() {
        Permanent forest = addEnchantedForest();

        harness.tapPermanent(player1, 0);

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrificing Wolfwillow Haven creates a 2/2 green Wolf token")
    void sacrificingCreatesWolfToken() {
        addEnchantedForest();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(wolf -> {
                    assertThat(wolf.getCard().getName()).isEqualTo("Wolf");
                    assertThat(wolf.getCard().getPower()).isEqualTo(2);
                    assertThat(wolf.getCard().getToughness()).isEqualTo(2);
                    assertThat(wolf.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(wolf.getCard().getSubtypes()).containsExactly(CardSubtype.WOLF);
                });
        harness.assertInGraveyard(player1, "Wolfwillow Haven");
    }

    @Test
    @DisplayName("The token ability cannot be activated during an opponent's turn")
    void cannotActivateOnOpponentTurn() {
        addEnchantedForest();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Wolfwillow Haven can enchant only a land")
    void cannotEnchantNonLand() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WolfwillowHaven()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    private Permanent addEnchantedForest() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WolfwillowHaven());
        aura.setAttachedTo(forest.getId());
        return forest;
    }
}
