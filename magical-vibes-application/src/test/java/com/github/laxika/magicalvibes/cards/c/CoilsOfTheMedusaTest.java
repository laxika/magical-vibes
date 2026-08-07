package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfCorpses;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CoilsOfTheMedusaTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/-1")
    void enchantedCreatureGetsBoost() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.setHand(player1, List.of(new CoilsOfTheMedusa()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing the Aura destroys the non-Wall creatures blocking the enchanted creature")
    void sacrificeDestroysNonWallBlockers() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachCoilsTo(bears);

        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new WallOfCorpses());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        harness.activateAbility(player1, 1, null, null);
        harness.assertInGraveyard(player1, "Coils of the Medusa");

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Wall of Corpses");
    }

    @Test
    @DisplayName("A creature blocking someone else survives")
    void otherBlockersAreUnaffected() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        attachCoilsTo(bears);

        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 1)));
        resolveAllTriggers();

        harness.activateAbility(player1, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    private void attachCoilsTo(Permanent host) {
        Permanent aura = new Permanent(new CoilsOfTheMedusa());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }
}
