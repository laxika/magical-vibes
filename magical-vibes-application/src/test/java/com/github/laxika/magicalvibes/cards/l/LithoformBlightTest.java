package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LithoformBlight.class, Forest.class, GrizzlyBears.class})
class LithoformBlightTest extends BaseCardTest {

    @Test
    @DisplayName("Lithoform Blight draws a card when it enters and attaches to the target land")
    void drawsAndAttaches() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new LithoformBlight()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).singleElement().isInstanceOf(GrizzlyBears.class);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof LithoformBlight
                        && forest.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Lithoform Blight removes land types and printed abilities and grants colorless mana")
    void removesLandTypesAndGrantsColorlessMana() {
        Permanent forest = addEnchantedForest();

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).isEmpty();
        assertThat(gqs.computeStaticBonus(gd, forest).losesAllAbilities()).isTrue();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isOne();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Lithoform Blight removes nonbasic land types as well")
    void removesNonbasicLandTypes() {
        Card caveCard = new Card();
        caveCard.setName("Test Cave");
        caveCard.setType(CardType.LAND);
        caveCard.setSubtypes(List.of(CardSubtype.CAVE));
        Permanent cave = harness.addToBattlefieldAndReturn(player1, caveCard);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new LithoformBlight());
        aura.setAttachedTo(cave.getId());

        assertThat(gqs.hasEffectiveSubtype(gd, cave, CardSubtype.CAVE)).isFalse();
    }

    @Test
    @DisplayName("Lithoform Blight's life-paid ability produces mana of the chosen color")
    void paysLifeForAnyColorMana() {
        Permanent forest = addEnchantedForest();
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isOne();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing Lithoform Blight restores the land's normal type and mana ability")
    void restoresLandWhenAuraLeaves() {
        Permanent forest = addEnchantedForest();
        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof LithoformBlight)
                .findFirst()
                .orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.FOREST);
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isOne();
    }

    @Test
    @DisplayName("Lithoform Blight cannot target a nonland permanent")
    void cannotTargetNonland() {
        harness.addToBattlefield(player1, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LithoformBlight()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    private Permanent addEnchantedForest() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new LithoformBlight());
        aura.setAttachedTo(forest.getId());
        return forest;
    }
}
