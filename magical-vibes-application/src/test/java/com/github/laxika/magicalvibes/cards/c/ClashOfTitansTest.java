package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ClashOfTitans.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class})
class ClashOfTitansTest extends BaseCardTest {

    @Test
    void targetCreaturesFightEachOther() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new ClashOfTitans()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, List.of(bearsId, giantId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        Permanent giant = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(giant.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void canTargetTwoCreaturesControlledByTheSamePlayer() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new ClashOfTitans()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, List.of(giantId, elvesId));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    void cannotTargetTheSameCreatureTwice() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ClashOfTitans()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bearsId, bearsId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different");
    }
}
