package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BerserkMurlodont.class, GrizzlyBears.class})
class BerserkMurlodontTest extends BaseCardTest {

    @Test
    @DisplayName("A blocked Beast gets +1/+1 for each creature blocking it")
    void blockedBeastGetsBonusForEachBlocker() {
        Permanent murlodont = addCreatureReady(player1, new BerserkMurlodont());
        murlodont.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(murlodont.getPowerModifier()).isEqualTo(2);
        assertThat(murlodont.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Berserk Murlodont does not trigger for a blocked non-Beast")
    void doesNotTriggerForBlockedNonBeast() {
        addCreatureReady(player1, new BerserkMurlodont());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(gd.stack).isEmpty();
        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
    }
}
