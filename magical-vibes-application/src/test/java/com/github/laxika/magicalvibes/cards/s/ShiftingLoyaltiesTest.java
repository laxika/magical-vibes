package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShiftingLoyaltiesTest extends BaseCardTest {

    private void prepare() {
        harness.setHand(player1, List.of(new ShiftingLoyalties()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    @Test
    @DisplayName("Exchanges control of two target creatures")
    void exchangesTwoCreatures() {
        prepare();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castAndResolveSorcery(player1, 0, List.of(first.getId(), second.getId()));

        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can exchange an artifact creature with a creature")
    void exchangesPermanentsSharingAType() {
        prepare();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Juggernaut());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castAndResolveSorcery(player1, 0, List.of(first.getId(), second.getId()));

        harness.assertOnBattlefield(player2, "Juggernaut");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Rejects targets that do not share a card type")
    void rejectsTargetsWithoutSharedCardType() {
        prepare();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, List.of(artifact.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("share a card type");
    }

    @Test
    @DisplayName("Does nothing when both permanents have the same controller")
    void doesNothingWhenBothHaveSameController() {
        prepare();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castAndResolveSorcery(player1, 0, List.of(first.getId(), second.getId()));

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }
}
