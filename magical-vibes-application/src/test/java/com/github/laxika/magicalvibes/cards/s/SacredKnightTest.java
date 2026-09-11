package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FeralShadow;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.r.RegalUnicorn;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SacredKnight.class, FeralShadow.class, HillGiant.class, RegalUnicorn.class})
class SacredKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Sacred Knight can't be blocked by a black creature")
    void cannotBeBlockedByBlackCreature() {
        attackWithKnight(new FeralShadow());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by");
    }

    @Test
    @DisplayName("Sacred Knight can't be blocked by a red creature")
    void cannotBeBlockedByRedCreature() {
        attackWithKnight(new HillGiant());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by");
    }

    @Test
    @DisplayName("Sacred Knight can't be blocked by a creature that is both black and red")
    void cannotBeBlockedByBlackAndRedCreature() {
        Permanent blackAndRedCreature = attackWithKnight(new FeralShadow());
        blackAndRedCreature.setColorOverridden(true);
        blackAndRedCreature.getTransientColors().addAll(Set.of(CardColor.BLACK, CardColor.RED));

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by");
    }

    @Test
    @DisplayName("Sacred Knight can be blocked by a creature that is neither black nor red")
    void canBeBlockedByNonBlackNonRedCreature() {
        Permanent unicorn = attackWithKnight(new RegalUnicorn());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(unicorn.isBlocking()).isTrue();
    }

    private Permanent attackWithKnight(Card blockerCard) {
        addCreatureReady(player1, new SacredKnight());
        Permanent blocker = addCreatureReady(player2, blockerCard);
        declareAttackers(List.of(0));
        return blocker;
    }
}
