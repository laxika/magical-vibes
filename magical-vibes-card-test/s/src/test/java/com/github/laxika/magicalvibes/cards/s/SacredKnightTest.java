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

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by");
    }

    @Test
    @DisplayName("Sacred Knight can't be blocked by a red creature")
    void cannotBeBlockedByRedCreature() {
        attackWithKnight(new HillGiant());

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

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by");
    }

    @Test
    @DisplayName("Sacred Knight can be blocked by a creature that is neither black nor red")
    void canBeBlockedByNonBlackNonRedCreature() {
        Permanent unicorn = attackWithKnight(new RegalUnicorn());

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(unicorn.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A white creature that is also black can't block Sacred Knight")
    void cannotBeBlockedByWhiteAndBlackCreature() {
        Permanent blocker = attackWithKnight(new RegalUnicorn());
        blocker.setColorOverridden(true);
        blocker.getTransientColors().addAll(Set.of(CardColor.WHITE, CardColor.BLACK));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by");
    }

    @Test
    @DisplayName("A white creature that is also red can't block Sacred Knight")
    void cannotBeBlockedByWhiteAndRedCreature() {
        Permanent blocker = attackWithKnight(new RegalUnicorn());
        blocker.setColorOverridden(true);
        blocker.getTransientColors().addAll(Set.of(CardColor.WHITE, CardColor.RED));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by");
    }

    @Test
    @DisplayName("A multicolored creature that is neither black nor red can block Sacred Knight")
    void canBeBlockedByWhiteAndGreenCreature() {
        Permanent blocker = attackWithKnight(new RegalUnicorn());
        blocker.setColorOverridden(true);
        blocker.getTransientColors().addAll(Set.of(CardColor.WHITE, CardColor.GREEN));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Sacred Knight can block a black attacker")
    void canBlockBlackCreature() {
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        attacker.setColorOverridden(true);
        attacker.getTransientColors().add(CardColor.BLACK);
        Permanent knight = addCreatureReady(player2, new SacredKnight());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(knight.isBlocking()).isTrue();
    }

    private Permanent attackWithKnight(Card blockerCard) {
        addCreatureReady(player1, new SacredKnight());
        Permanent blocker = addCreatureReady(player2, blockerCard);
        declareAttackersAndPrepareBlockers(List.of(0));
        return blocker;
    }
}
