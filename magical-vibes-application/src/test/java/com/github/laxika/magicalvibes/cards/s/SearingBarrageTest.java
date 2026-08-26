package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SearingBarrage.class, GrizzlyBears.class, Forest.class})
class SearingBarrageTest extends BaseCardTest {

    @Test
    void dealsFiveDamageWithoutAdamant() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SearingBarrage()));
        addMana(4, 1);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void adamantAlsoDealsThreeDamageToTheCreatureController() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SearingBarrage()));
        addMana(2, 3);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    void cannotTargetANonCreaturePermanent() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new SearingBarrage()));
        addMana(4, 1);

        UUID targetId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana(int colorless, int red) {
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
        harness.addMana(player1, ManaColor.RED, red);
    }
}
