package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Electrify.class, DuneBeetle.class, Colossapede.class})
class ElectrifyTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage, killing a small creature")
    void killsSmallCreature() {
        harness.addToBattlefield(player2, new DuneBeetle());
        UUID targetId = harness.getPermanentId(player2, "Dune Beetle");
        harness.setHand(player1, List.of(new Electrify()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Dune Beetle");
        harness.assertInGraveyard(player2, "Dune Beetle");
    }

    @Test
    @DisplayName("Deals 4 damage to a large creature that survives")
    void damagesSurvivingCreature() {
        Permanent colossapede = harness.addToBattlefieldAndReturn(player2, new Colossapede());
        UUID targetId = colossapede.getId();
        harness.setHand(player1, List.of(new Electrify()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(colossapede.getMarkedDamage()).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Colossapede");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new Electrify()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target a creature controlled by the caster")
    void canTargetOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        harness.setHand(player1, List.of(new Electrify()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getMarkedDamage()).isEqualTo(4);
        harness.assertOnBattlefield(player1, "Colossapede");
        harness.assertInGraveyard(player1, "Electrify");
    }

    @Test
    @DisplayName("Does not damage another creature when its target dies before resolution")
    void targetDiesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DuneBeetle());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new Colossapede());
        harness.setHand(player1, List.of(new Electrify(), new Electrify()));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.assertInGraveyard(player2, "Dune Beetle");
        harness.passBothPriorities();

        assertThat(other.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Colossapede");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof Electrify).hasSize(2);
    }
}
