package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CavesOfKoilos;
import com.github.laxika.magicalvibes.cards.d.Dodecapod;
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

@CardUsed({ManaclesOfDecay.class, CavesOfKoilos.class, Dodecapod.class})
class ManaclesOfDecayTest extends BaseCardTest {

    @Test
    @DisplayName("Manacles of Decay can target a creature")
    void canTargetCreature() {
        Permanent creature = addCreatureReady(player2, new Dodecapod());
        ManaclesOfDecay aura = new ManaclesOfDecay();
        harness.setHand(player1, List.of(aura));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == aura && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Manacles of Decay cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new CavesOfKoilos());
        harness.setHand(player1, List.of(new ManaclesOfDecay()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanted creature cannot attack")
    void enchantedCreatureCannotAttack() {
        Permanent creature = addCreatureReady(player1, new Dodecapod());
        addAuraOn(creature, player1);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Enchanted creature can still block")
    void enchantedCreatureCanBlock() {
        Permanent blocker = addCreatureReady(player1, new Dodecapod());
        addAuraOn(blocker, player1);
        addCreatureReady(player2, new Dodecapod());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Black ability gives enchanted creature -1/-1 until end of turn")
    void blackAbilityShrinksEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new Dodecapod());
        addAuraOn(creature, player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Red ability makes enchanted creature unable to block this turn")
    void redAbilityPreventsBlockingUntilEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new Dodecapod());
        addAuraOn(creature, player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        assertThat(creature.isCantBlockThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.isCantBlockThisTurn()).isFalse();
    }

    private Permanent addAuraOn(Permanent enchanted, com.github.laxika.magicalvibes.model.Player controller) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new ManaclesOfDecay());
        aura.setAttachedTo(enchanted.getId());
        return aura;
    }
}
