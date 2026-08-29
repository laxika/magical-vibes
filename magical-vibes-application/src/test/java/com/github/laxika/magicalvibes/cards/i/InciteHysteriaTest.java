package com.github.laxika.magicalvibes.cards.i;

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

@CardUsed({InciteHysteria.class, GrizzlyBears.class, HillGiant.class, Ornithopter.class})
class InciteHysteriaTest extends BaseCardTest {

    @Test
    @DisplayName("Target and every creature sharing a color with it can't block this turn")
    void targetAndColorSharingCreaturesCantBlock() {
        Permanent target = addReadyCreature(player1, new GrizzlyBears());
        Permanent ownMatchingCreature = addReadyCreature(player1, new GrizzlyBears());
        Permanent opponentMatchingCreature = addReadyCreature(player2, new GrizzlyBears());
        Permanent differentColorCreature = addReadyCreature(player2, new HillGiant());
        Permanent colorlessCreature = addReadyCreature(player2, new Ornithopter());

        castInciteHysteria(target);

        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(ownMatchingCreature.isCantBlockThisTurn()).isTrue();
        assertThat(opponentMatchingCreature.isCantBlockThisTurn()).isTrue();
        assertThat(differentColorCreature.isCantBlockThisTurn()).isFalse();
        assertThat(colorlessCreature.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("A colorless target affects only itself")
    void colorlessTargetOnlyAffectsItself() {
        Permanent target = addReadyCreature(player1, new Ornithopter());
        Permanent otherColorlessCreature = addReadyCreature(player2, new Ornithopter());
        Permanent coloredCreature = addReadyCreature(player2, new GrizzlyBears());

        castInciteHysteria(target);

        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(otherColorlessCreature.isCantBlockThisTurn()).isFalse();
        assertThat(coloredCreature.isCantBlockThisTurn()).isFalse();
    }

    private void castInciteHysteria(Permanent target) {
        harness.setHand(player1, List.of(new InciteHysteria()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
