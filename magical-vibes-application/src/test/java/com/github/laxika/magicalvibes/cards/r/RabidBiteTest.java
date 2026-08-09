package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RabidBiteTest extends BaseCardTest {

    @Test
    @DisplayName("Controlled creature deals damage equal to its power to an opposing creature")
    void controlledCreatureDealsPowerDamageToOpposingCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new RabidBite()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castSorcery(player1, 0, List.of(bearId, elvesId));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Damage is dealt without the controlled creature taking damage")
    void damageIsOneSided() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new RabidBite()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID sourceId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");
        harness.castSorcery(player1, 0, List.of(sourceId, targetId));
        harness.passBothPriorities();

        Permanent source = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(source.getMarkedDamage()).isZero();
        Permanent target = harness.getGameData().playerBattlefields.get(player2.getId()).getFirst();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Targets must be a controlled creature and an opposing creature")
    void enforcesTargetRestrictions() {
        GrizzlyBears ownBear = new GrizzlyBears();
        GrizzlyBears ownOtherBear = new GrizzlyBears();
        harness.addToBattlefield(player1, ownBear);
        harness.addToBattlefield(player1, ownOtherBear);
        harness.setHand(player1, List.of(new RabidBite()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID firstId = harness.getGameData().playerBattlefields.get(player1.getId()).get(0).getId();
        UUID secondId = harness.getGameData().playerBattlefields.get(player1.getId()).get(1).getId();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(firstId, secondId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("don't control");
    }
}
