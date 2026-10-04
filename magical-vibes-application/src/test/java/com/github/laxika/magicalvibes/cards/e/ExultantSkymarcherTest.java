package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.o.OrazcaRaptor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExultantSkymarcher.class, OrazcaRaptor.class})
class ExultantSkymarcherTest extends BaseCardTest {

    @Test
    @DisplayName("Exultant Skymarcher can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent skymarcher = addReadyCreature(player2, new ExultantSkymarcher());

        Permanent attacker = addReadyCreature(player1, new ExultantSkymarcher());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(skymarcher.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Exultant Skymarcher can block a creature without flying")
    void canBlockNonFlyingCreature() {
        Permanent skymarcher = addReadyCreature(player2, new ExultantSkymarcher());

        Permanent attacker = addReadyCreature(player1, new OrazcaRaptor());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(skymarcher.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature without flying cannot block Exultant Skymarcher")
    void cannotBeBlockedByNonFlyingCreature() {
        Permanent skymarcher = addReadyCreature(player1, new ExultantSkymarcher());
        skymarcher.setAttacking(true);

        addReadyCreature(player2, new OrazcaRaptor());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }
}
