package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BrutalNightstalker;
import com.github.laxika.magicalvibes.cards.v.VolunteerMilitia;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProwlingNightstalker.class, VolunteerMilitia.class, BrutalNightstalker.class})
class ProwlingNightstalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Prowling Nightstalker cannot be blocked by a non-black creature")
    void cannotBeBlockedByNonBlackCreature() {
        Permanent nightstalker = attackingNightstalker();

        addCreatureReady(player2, new VolunteerMilitia());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by black creatures");
    }

    @Test
    @DisplayName("Prowling Nightstalker can be blocked by a black creature")
    void canBeBlockedByBlackCreature() {
        Permanent nightstalker = attackingNightstalker();

        Permanent blocker = addCreatureReady(player2, new BrutalNightstalker());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent attackingNightstalker() {
        Permanent nightstalker = addCreatureReady(player1, new ProwlingNightstalker());
        nightstalker.setAttacking(true);
        return nightstalker;
    }
}
