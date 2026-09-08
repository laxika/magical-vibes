package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChoArrimLegateTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for free when an opponent controls a Swamp and you control a Plains")
    void castsForAlternateCost() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Swamp());
        harness.setHand(player1, List.of(new ChoArrimLegate()));

        harness.castWithAlternateCost(player1, 0, (UUID) null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cho-Arrim Legate");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot use the alternate cost without an opponent-controlled Swamp")
    void alternateCostRequiresOpponentSwamp() {
        harness.addToBattlefield(player1, new Plains());
        harness.setHand(player1, List.of(new ChoArrimLegate()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, (UUID) null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot use the alternate cost without a controller-controlled Plains")
    void alternateCostRequiresControllerPlains() {
        harness.addToBattlefield(player2, new Swamp());
        harness.setHand(player1, List.of(new ChoArrimLegate()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, (UUID) null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Has protection from black")
    void hasProtectionFromBlack() {
        Permanent legate = harness.addToBattlefieldAndReturn(player1, new ChoArrimLegate());

        assertThat(gqs.hasProtectionFrom(gd, legate, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, legate, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Can be cast normally for its mana cost")
    void castsNormally() {
        harness.setHand(player1, List.of(new ChoArrimLegate()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cho-Arrim Legate");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
