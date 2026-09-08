package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.StriderHarness;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuriokWindwalkerTest extends BaseCardTest {

    @Test
    void attachesTargetEquipmentToTargetCreature() {
        Permanent windwalker = addReadyWindwalker(player1);
        Permanent equipment = addEquipment(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(equipment.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(windwalker.isTapped()).isTrue();
    }

    @Test
    void canMoveEquipmentBetweenCreatures() {
        addReadyWindwalker(player1);
        Permanent equipment = addEquipment(player1);
        Permanent firstCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player1, new GrizzlyBears());
        equipment.setAttachedTo(firstCreature.getId());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(equipment.getId(), secondCreature.getId()));
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(secondCreature.getId());
    }

    @Test
    void cannotTargetAnOpponentEquipment() {
        addReadyWindwalker(player1);
        Permanent opponentEquipment = addEquipment(player2);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(opponentEquipment.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyWindwalker(Player player) {
        Permanent permanent = new Permanent(new AuriokWindwalker());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addEquipment(Player player) {
        Permanent permanent = new Permanent(new StriderHarness());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
