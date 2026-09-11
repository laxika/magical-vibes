package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AcquisitionOctopus.class, GrizzlyBears.class})
class AcquisitionOctopusTest extends BaseCardTest {

    @Test
    void unconfiguredOctopusDrawsWhenItDealsCombatDamage() {
        Permanent octopus = addCreatureReady(player1, new AcquisitionOctopus());
        octopus.setAttacking(true);
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    void equippedCreatureDrawsWhenItDealsCombatDamage() {
        Permanent octopus = addCreatureReady(player1, new AcquisitionOctopus());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        octopus.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    void reconfigureAttachesAndUnattachesTheOctopus() {
        Permanent octopus = addCreatureReady(player1, new AcquisitionOctopus());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(octopus.getAttachedTo()).isEqualTo(creature.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(octopus.getAttachedTo()).isNull();
    }

    @Test
    void reconfigureCannotTargetAnOpponentsCreature() {
        Permanent octopus = addCreatureReady(player1, new AcquisitionOctopus());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(octopus.getAttachedTo()).isNull();
    }
}
