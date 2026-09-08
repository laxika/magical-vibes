package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RallyTheRighteous.class, GrizzlyBears.class, HillGiant.class, Ornithopter.class})
class RallyTheRighteousTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps and boosts the target and every creature sharing a color with it")
    void untapsAndBoostsTargetAndColorSharingCreatures() {
        Permanent target = addTappedCreature(player1, new GrizzlyBears());
        Permanent ownMatchingCreature = addTappedCreature(player1, new GrizzlyBears());
        Permanent opponentMatchingCreature = addTappedCreature(player2, new GrizzlyBears());
        Permanent differentColorCreature = addTappedCreature(player2, new HillGiant());

        castRally(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(ownMatchingCreature.isTapped()).isFalse();
        assertThat(opponentMatchingCreature.isTapped()).isFalse();
        assertThat(differentColorCreature.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, ownMatchingCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentMatchingCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, differentColorCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("A colorless target does not affect other colorless creatures")
    void colorlessTargetOnlyAffectsItself() {
        Permanent target = addTappedCreature(player1, new Ornithopter());
        Permanent otherColorlessCreature = addTappedCreature(player2, new Ornithopter());
        Permanent coloredCreature = addTappedCreature(player2, new GrizzlyBears());

        castRally(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(otherColorlessCreature.isTapped()).isTrue();
        assertThat(coloredCreature.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, otherColorlessCreature)).isZero();
        assertThat(gqs.getEffectivePower(gd, coloredCreature)).isEqualTo(2);
    }

    private void castRally(Permanent target) {
        harness.setHand(player1, List.of(new RallyTheRighteous()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addTappedCreature(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.tap();
        return permanent;
    }
}
