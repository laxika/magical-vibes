package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DrakeHatchling;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RishadanAirship.class, DrakeHatchling.class, FreshVolunteers.class})
class RishadanAirshipTest extends BaseCardTest {

    @Test
    @DisplayName("Rishadan Airship can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent airship = addCreatureReady(player2, new RishadanAirship());
        Permanent attacker = addCreatureReady(player1, new DrakeHatchling());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(airship.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Rishadan Airship cannot block a creature without flying")
    void cannotBlockNonFlyingCreature() {
        addCreatureReady(player2, new RishadanAirship());
        Permanent attacker = addCreatureReady(player1, new FreshVolunteers());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }
}
