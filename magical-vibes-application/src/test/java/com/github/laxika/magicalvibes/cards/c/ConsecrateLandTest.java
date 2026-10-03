package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.cards.w.WildGrowth;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConsecrateLand.class, Forest.class, GrizzlyBears.class, StoneRain.class, WildGrowth.class})
class ConsecrateLandTest extends BaseCardTest {

    @Test
    @DisplayName("Consecrate Land gives the enchanted land indestructible")
    void givesEnchantedLandIndestructible() {
        Permanent forest = addForest();
        castConsecrateLand(forest);

        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Indestructible enchanted land survives a destroy effect")
    void enchantedLandSurvivesDestruction() {
        Permanent forest = addForest();
        castConsecrateLand(forest);

        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, forest.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(forest);
    }

    @Test
    @DisplayName("Enchanted land cannot be enchanted by another Aura")
    void cannotBeEnchantedByAnotherAura() {
        Permanent forest = addForest();
        castConsecrateLand(forest);

        harness.setHand(player1, List.of(new WildGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Consecrate Land can target only a land")
    void cannotTargetNonLand() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ConsecrateLand()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Consecrate Land puts an existing Aura into its owner's graveyard")
    void removesExistingAura() {
        Permanent forest = addForest();
        harness.setHand(player1, List.of(new WildGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Wild Growth");

        castConsecrateLand(forest);

        harness.assertNotOnBattlefield(player1, "Wild Growth");
        harness.assertInGraveyard(player1, "Wild Growth");
        harness.assertOnBattlefield(player1, "Consecrate Land");
        assertThat(findPermanent(player1, "Consecrate Land").getAttachedTo()).isEqualTo(forest.getId());
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Consecrate Land removes an opponent's Aura from an opponent's land")
    void removesOpponentsExistingAura() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent growth = harness.addToBattlefieldAndReturn(player2, new WildGrowth());
        growth.setAttachedTo(forest.getId());

        castConsecrateLand(forest);

        harness.assertNotOnBattlefield(player2, "Wild Growth");
        harness.assertInGraveyard(player2, "Wild Growth");
        harness.assertNotInGraveyard(player1, "Wild Growth");
        harness.assertOnBattlefield(player1, "Consecrate Land");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(forest);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("A second Consecrate Land cannot target the enchanted land")
    void cannotBeEnchantedBySecondConsecrateLand() {
        Permanent forest = addForest();
        castConsecrateLand(forest);
        harness.setHand(player1, List.of(new ConsecrateLand()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addForest() {
        return harness.addToBattlefieldAndReturn(player1, new Forest());
    }

    private void castConsecrateLand(Permanent forest) {
        harness.setHand(player1, List.of(new ConsecrateLand()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
    }
}
