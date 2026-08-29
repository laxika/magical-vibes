package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GreaterWerewolf;
import com.github.laxika.magicalvibes.cards.h.HowlpackWolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SilverfurPartisanTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Wolf token when a Wolf becomes the target of an instant")
    void createsTokenForTargetedWolf() {
        harness.addToBattlefield(player1, new SilverfurPartisan());
        harness.addToBattlefield(player1, new HowlpackWolf());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Howlpack Wolf"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Wolf")).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates a Wolf token when a Werewolf becomes the target of an instant")
    void createsTokenForTargetedWerewolf() {
        harness.addToBattlefield(player1, new SilverfurPartisan());
        harness.addToBattlefield(player1, new GreaterWerewolf());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Greater Werewolf"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Wolf")).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not create a token when a non-Wolf creature becomes the target")
    void doesNotCreateTokenForOtherCreature() {
        harness.addToBattlefield(player1, new SilverfurPartisan());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Wolf")).isZero();
    }
}
