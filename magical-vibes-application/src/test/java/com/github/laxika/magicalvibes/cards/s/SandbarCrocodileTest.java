package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SandbarCrocodile.class)
class SandbarCrocodileTest extends BaseCardTest {

    @Test
    @DisplayName("Phasing phases Sandbar Crocodile out during its controller's untap step")
    void phasesOutDuringControllersUntapStep() {
        Permanent crocodile = harness.addToBattlefieldAndReturn(player1, new SandbarCrocodile());

        advanceToUpkeep(player2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(crocodile);

        advanceToUpkeep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(crocodile);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(crocodile);
    }

    @Test
    @DisplayName("Phased-out Sandbar Crocodile returns during its controller's next untap step")
    void phasesBackInDuringNextControllersUntapStep() {
        Permanent crocodile = harness.addToBattlefieldAndReturn(player1, new SandbarCrocodile());

        advanceToUpkeep(player1);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(crocodile);

        advanceToUpkeep(player2);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(crocodile);

        advanceToUpkeep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(crocodile);
        assertThat(gd.phasedOutPermanents.getOrDefault(player1.getId(), List.of()))
                .doesNotContain(crocodile);
    }
}
