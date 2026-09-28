package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Frostling;
import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.i.IshiIshiAkkiCrackshot;
import com.github.laxika.magicalvibes.cards.s.Shuko;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeartOfLight.class, Frostling.class, GnarledMass.class, IshiIshiAkkiCrackshot.class, Shuko.class})
class HeartOfLightTest extends BaseCardTest {

    @Test
    @DisplayName("Can target a creature with Heart of Light")
    void canTargetCreature() {
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        harness.setHand(player1, List.of(new HeartOfLight()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Heart of Light")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new Shuko());
        harness.setHand(player1, List.of(new HeartOfLight()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        Permanent artifact = findPermanent(player1, "Shuko");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Prevents combat damage to and dealt by the enchanted creature")
    void preventsCombatDamageToAndByEnchantedCreature() {
        Permanent enchantedCreature = addCreatureReady(player1, new GnarledMass());
        castHeartOfLight(enchantedCreature);
        Permanent blocker = addCreatureReady(player2, new Frostling());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player1);

        assertThat(enchantedCreature.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Gnarled Mass");
        harness.assertOnBattlefield(player2, "Frostling");
    }

    @Test
    @DisplayName("Prevents noncombat damage dealt to the enchanted creature")
    void preventsNoncombatDamageToEnchantedCreature() {
        Permanent enchantedCreature = addCreatureReady(player1, new GnarledMass());
        castHeartOfLight(enchantedCreature);
        addCreatureReady(player2, new Frostling());

        harness.activateAbility(player2, 0, null, enchantedCreature.getId());
        harness.passBothPriorities();

        assertThat(enchantedCreature.getMarkedDamage()).isZero();
        harness.assertNotOnBattlefield(player2, "Frostling");
        harness.assertInGraveyard(player2, "Frostling");
    }

    @Test
    @DisplayName("Prevents noncombat damage dealt by the enchanted creature")
    void preventsNoncombatDamageByEnchantedCreature() {
        Permanent enchantedCreature = addCreatureReady(player1, new IshiIshiAkkiCrackshot());
        castHeartOfLight(enchantedCreature);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Frostling()));
        harness.addMana(player2, ManaColor.RED, 1);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    private void castHeartOfLight(Permanent enchantedCreature) {
        harness.setHand(player1, List.of(new HeartOfLight()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, enchantedCreature.getId());
        harness.passBothPriorities();
    }
}
