package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChandrasRevolutionTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to a creature and taps a land that skips its next untap")
    void damagesCreatureAndLocksLand() {
        Permanent creature = addCreatureReady(player2, new ColossalDreadmaw());
        Permanent land = addReadyLand(player2, new Mountain());

        castRevolution(creature.getId(), land.getId());

        assertThat(creature.getMarkedDamage()).isEqualTo(4);
        assertThat(land.isTapped()).isTrue();
        assertThat(land.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Requires a creature target followed by a land target")
    void rejectsWrongTargetTypes() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent land = addReadyLand(player2, new Forest());

        harness.setHand(player1, List.of(new ChandrasRevolution()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(land.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castRevolution(UUID creatureId, UUID landId) {
        harness.setHand(player1, List.of(new ChandrasRevolution()));
        addMana();
        harness.castSorcery(player1, 0, List.of(creatureId, landId));
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private Permanent addReadyLand(Player owner, Card land) {
        harness.addToBattlefield(owner, land);
        Permanent permanent = findPermanent(owner, land.getName());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
