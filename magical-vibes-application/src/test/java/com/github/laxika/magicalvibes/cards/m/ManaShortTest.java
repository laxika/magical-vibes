package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ManaShortTest extends BaseCardTest {

    @Test
    @DisplayName("Taps all lands target player controls")
    void tapsAllLands() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        List<Permanent> battlefield = gd.playerBattlefields.get(player2.getId());
        assertThat(battlefield).allMatch(p -> !p.isTapped());

        castAndResolve(player2.getId());

        assertThat(battlefield).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Target player loses all unspent mana")
    void emptiesTargetManaPool() {
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.addMana(player2, ManaColor.BLUE, 2);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isEqualTo(5);

        castAndResolve(player2.getId());

        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Does not tap non-land permanents")
    void doesNotTapNonLands() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();

        castAndResolve(player2.getId());

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not tap caster's lands when targeting opponent")
    void doesNotAffectCaster() {
        harness.addToBattlefield(player1, new Forest());
        Permanent casterLand = gd.playerBattlefields.get(player1.getId()).getFirst();

        castAndResolve(player2.getId());

        assertThat(casterLand.isTapped()).isFalse();
    }

    private void castAndResolve(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new ManaShort()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, targetPlayerId);
        harness.passBothPriorities();
    }
}
