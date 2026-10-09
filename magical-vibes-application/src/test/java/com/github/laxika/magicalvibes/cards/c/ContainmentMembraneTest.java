package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BlindingDrone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ContainmentMembrane.class, GrizzlyBears.class, BlindingDrone.class})
class ContainmentMembraneTest extends BaseCardTest {

    @Test
    void resolvingAttachesToTargetCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ContainmentMembrane()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Containment Membrane")
                        && permanent.isAttached()
                        && permanent.getAttachedTo().equals(creature.getId()));
    }

    @Test
    void enchantedCreatureDoesNotUntapDuringItsControllersUntapStep() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();

        Permanent membrane = new Permanent(new ContainmentMembrane());
        membrane.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(membrane);

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void creatureUntapsAfterContainmentMembraneLeavesBattlefield() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();

        Permanent membrane = new Permanent(new ContainmentMembrane());
        membrane.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(membrane);
        gd.playerBattlefields.get(player1.getId()).remove(membrane);

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @CardUsed({ContainmentMembrane.class, BlindingDrone.class})
    void canBeCastForOneBlueAfterCastingAnotherSpellThisTurn() {
        harness.setHand(player1, List.of(new BlindingDrone(), new ContainmentMembrane()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent creature = findPermanent(player1, "Blinding Drone");
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castWithAlternateCost(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Containment Membrane").getAttachedTo())
                .isEqualTo(creature.getId());
    }

    @Test
    @CardUsed({ContainmentMembrane.class, BlindingDrone.class})
    void resolvingDoesNotTapCreatureAndOnlyEnchantedCreatureIsLocked() {
        Permanent creature = addCreatureReady(player2, new BlindingDrone());
        Permanent otherCreature = addCreatureReady(player2, new BlindingDrone());
        otherCreature.tap();
        harness.setHand(player1, List.of(new ContainmentMembrane()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        creature.tap();
        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
        assertThat(otherCreature.isTapped()).isFalse();
    }
}
