package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VastwoodAnimistTest extends BaseCardTest {

    @Test
    @DisplayName("Animating a land makes it an X/X Elemental creature that is still a land")
    void animatesLandAsElemental() {
        addReadyAnimist(player1);
        Permanent forest = addForest(player1);

        activateAnimist(forest);

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(1);
        assertThat(forest.getTransientSubtypes()).contains(CardSubtype.ELEMENTAL);
    }

    @Test
    @DisplayName("Animation scales with the number of Allies controlled")
    void animationScalesWithAllyCount() {
        addReadyAnimist(player1);
        addAnimist(player1);
        Permanent forest = addForest(player1);

        activateAnimist(forest);

        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(2);
    }

    @Test
    @DisplayName("Animation wears off at end of turn")
    void animationWearsOff() {
        addReadyAnimist(player1);
        Permanent forest = addForest(player1);

        activateAnimist(forest);
        forest.resetModifiers();

        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(forest.getTransientSubtypes()).doesNotContain(CardSubtype.ELEMENTAL);
    }

    @Test
    @DisplayName("Cannot target a land controlled by an opponent")
    void cannotTargetOpponentsLand() {
        addReadyAnimist(player1);
        Permanent forest = addForest(player2);

        assertThatThrownBy(() -> activateAnimist(forest))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyAnimist(Player player) {
        Permanent perm = addAnimist(player);
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addAnimist(Player player) {
        Permanent perm = new Permanent(new VastwoodAnimist());
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addForest(Player player) {
        Permanent perm = new Permanent(new Forest());
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void activateAnimist(Permanent forest) {
        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();
    }
}
