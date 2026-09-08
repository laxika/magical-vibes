package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.k.KjeldoranSkyknight;
import com.github.laxika.magicalvibes.cards.k.KjeldoranWarrior;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WindSpirit.class, KjeldoranWarrior.class, KjeldoranSkyknight.class})
class WindSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Flying lets Wind Spirit deal combat damage past a ground creature")
    void flyingLetsWindSpiritDealDamagePastGroundCreature() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new WindSpirit());
        addCreatureReady(player2, new KjeldoranWarrior());

        declareAttackers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Flying prevents a ground creature from blocking Wind Spirit")
    void flyingPreventsGroundCreatureFromBlocking() {
        Permanent attacker = addCreatureReady(player1, new WindSpirit());
        addCreatureReady(player2, new KjeldoranWarrior());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block Wind Spirit (flying)");
    }

    @Test
    @DisplayName("Menace prevents one flying creature from blocking Wind Spirit")
    void menacePreventsOneFlyingCreatureFromBlocking() {
        Permanent attacker = addCreatureReady(player1, new WindSpirit());
        addCreatureReady(player2, new KjeldoranSkyknight());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by two or more creatures");
    }

    @Test
    @DisplayName("Menace allows two flying creatures to block Wind Spirit")
    void menaceAllowsTwoFlyingCreaturesToBlock() {
        Permanent attacker = addCreatureReady(player1, new WindSpirit());
        Permanent firstBlocker = addCreatureReady(player2, new KjeldoranSkyknight());
        Permanent secondBlocker = addCreatureReady(player2, new KjeldoranSkyknight());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }
}
