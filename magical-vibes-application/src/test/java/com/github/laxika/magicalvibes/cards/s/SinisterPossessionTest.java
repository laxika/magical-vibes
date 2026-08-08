package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SinisterPossessionTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature attacking makes its controller lose 2 life")
    void attackingLosesTwoLife() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachPossession(bears);

        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Enchanted creature blocking makes its controller lose 2 life")
    void blockingLosesTwoLife() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        attachPossession(blocker);

        int lifeBefore = gd.getLife(player2.getId());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("An unenchanted creature attacking costs no life")
    void unenchantedAttackerLosesNoLife() {
        addCreatureReady(player1, new GrizzlyBears());

        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new SinisterPossession()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void attachPossession(Permanent creature) {
        Permanent aura = new Permanent(new SinisterPossession());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }
}
