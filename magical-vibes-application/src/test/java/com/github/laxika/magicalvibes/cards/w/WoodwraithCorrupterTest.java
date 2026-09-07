package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WoodwraithCorrupter.class, Forest.class, Mountain.class})
class WoodwraithCorrupterTest extends BaseCardTest {

    @Test
    @DisplayName("Target Forest becomes a permanent 4/4 black and green Elemental Horror")
    void targetForestBecomesPermanentAnimation() {
        addCorrupter(player1);
        Permanent forest = addForest(player1);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, forest))
                .containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
        assertThat(forest.getGrantedSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.ELEMENTAL, CardSubtype.HORROR);
    }

    @Test
    @DisplayName("Permanent Forest animation survives cleanup")
    void animationSurvivesCleanup() {
        addCorrupter(player1);
        Permanent forest = addForest(player1);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();
        forest.resetModifiers();

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, forest))
                .containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
    }

    @Test
    @DisplayName("Cannot target a non-Forest land")
    void cannotTargetNonForest() {
        addCorrupter(player1);
        Permanent mountain = new Permanent(new Mountain());
        mountain.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(mountain);
        addActivationMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Forest");
    }

    private Permanent addCorrupter(Player player) {
        Permanent permanent = new Permanent(new WoodwraithCorrupter());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addForest(Player player) {
        Permanent permanent = new Permanent(new Forest());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addActivationMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
    }
}
