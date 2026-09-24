package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KelsienThePlague.class, GrizzlyBears.class, LlanowarElves.class})
class KelsienThePlagueTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each experience counter its controller has")
    void getsPlusOnePlusOneForEachExperienceCounter() {
        gd.playerExperienceCounters.put(player1.getId(), 2);
        harness.addToBattlefield(player1, new KelsienThePlague());

        Permanent kelsien = findPermanent(player1, "Kelsien, the Plague");
        assertThat(gqs.getEffectivePower(gd, kelsien)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, kelsien)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets an experience counter when the damaged creature dies this turn")
    void getsExperienceCounterWhenDamagedCreatureDies() {
        harness.addToBattlefield(player1, new KelsienThePlague());
        harness.addToBattlefield(player2, new LlanowarElves());

        UUID targetId = findPermanent(player2, "Llanowar Elves").getId();
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.playerExperienceCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Can register the experience trigger for a creature that dies later this turn")
    void getsExperienceCounterWhenTargetDiesLater() {
        harness.addToBattlefield(player1, new KelsienThePlague());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = findPermanent(player2, "Grizzly Bears").getId();
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();
        assertThat(gd.playerExperienceCounters).doesNotContainKey(player1.getId());

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature its controller controls")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new KelsienThePlague());
        harness.addToBattlefield(player1, new GrizzlyBears());

        UUID targetId = findPermanent(player1, "Grizzly Bears").getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }
}
