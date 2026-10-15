package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BenalishInfantry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BenalishInfantry.class, CinderWall.class, CoilsOfTheMedusa.class})
class CoilsOfTheMedusaTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/-1")
    void enchantedCreatureGetsBoost() {
        Permanent infantry = addCreatureReady(player1, new BenalishInfantry());

        harness.setHand(player1, List.of(new CoilsOfTheMedusa()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, infantry.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, infantry)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, infantry)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrificing the Aura destroys all non-Wall creatures blocking the enchanted creature")
    void sacrificeDestroysAllNonWallBlockers() {
        Permanent attacker = addCreatureReady(player1, new BenalishInfantry());
        attachCoilsTo(attacker);

        addCreatureReady(player2, new BenalishInfantry());
        addCreatureReady(player2, new BenalishInfantry());
        addCreatureReady(player2, new CinderWall());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)));

        harness.activateAbility(player1, 1, null, null);
        harness.assertInGraveyard(player1, "Coils of the Medusa");

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Cinder Wall");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Benalish Infantry"))
                .hasSize(2);
    }

    @Test
    @DisplayName("A creature blocking someone else survives")
    void otherBlockersAreUnaffected() {
        Permanent firstAttacker = addCreatureReady(player1, new BenalishInfantry());
        addCreatureReady(player1, new BenalishInfantry());
        attachCoilsTo(firstAttacker);

        addCreatureReady(player2, new BenalishInfantry());
        addCreatureReady(player2, new BenalishInfantry());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 1)));

        harness.activateAbility(player1, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The Aura can be sacrificed without blockers and its boost ends immediately")
    void sacrificeWithoutBlockersRemovesBoost() {
        Permanent host = addCreatureReady(player1, new BenalishInfantry());
        Permanent otherCreature = addCreatureReady(player2, new BenalishInfantry());
        attachCoilsTo(host);

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(2);

        harness.activateAbility(player1, 1, null, null);

        harness.assertInGraveyard(player1, "Coils of the Medusa");
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(3);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(host);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(otherCreature);
    }

    @Test
    @DisplayName("The Aura's controller can destroy their own creatures blocking an opponent's enchanted creature")
    void destroysBlockersOfOpponentsEnchantedCreature() {
        Permanent attacker = addCreatureReady(player2, new BenalishInfantry());
        addCreatureReady(player1, new BenalishInfantry());
        attachCoilsTo(attacker);

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Coils of the Medusa");
        harness.assertInGraveyard(player1, "Benalish Infantry");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(attacker);
    }

    private void attachCoilsTo(Permanent host) {
        Permanent aura = new Permanent(new CoilsOfTheMedusa());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }
}
