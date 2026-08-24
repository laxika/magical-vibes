package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Icequake;
import com.github.laxika.magicalvibes.cards.t.TraceOfAbundance;
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

@CardUsed({ConsecrateLand.class, Forest.class, GrizzlyBears.class, Icequake.class, TraceOfAbundance.class})
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

        harness.setHand(player1, List.of(new Icequake()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(forest);
    }

    @Test
    @DisplayName("Enchanted land cannot be enchanted by another Aura")
    void cannotBeEnchantedByAnotherAura() {
        Permanent forest = addForest();
        castConsecrateLand(forest);

        harness.setHand(player1, List.of(new TraceOfAbundance()));
        harness.addMana(player1, ManaColor.GREEN, 2);

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

    private Permanent addForest() {
        harness.addToBattlefield(player1, new Forest());
        return findPermanent(player1, "Forest");
    }

    private void castConsecrateLand(Permanent forest) {
        harness.setHand(player1, List.of(new ConsecrateLand()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
    }
}
