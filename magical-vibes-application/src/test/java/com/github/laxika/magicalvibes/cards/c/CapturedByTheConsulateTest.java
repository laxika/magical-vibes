package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.p.PeelFromReality;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CapturedByTheConsulateTest extends BaseCardTest {

    @Test
    @DisplayName("Captured by the Consulate can enchant only an opponent's creature")
    void canEnchantOnlyOpponentsCreature() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new CapturedByTheConsulate()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Enchanted creature cannot attack")
    void enchantedCreatureCannotAttack() {
        Permanent enchantedCreature = addCreatureReady(player2, new GrizzlyBears());
        castAuraOn(enchantedCreature);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("A single-target opponent spell is redirected to the enchanted creature")
    void redirectsSingleTargetSpell() {
        Permanent originalTarget = addCreatureReady(player1, new GrizzlyBears());
        Permanent enchantedCreature = addCreatureReady(player2, new GrizzlyBears());
        castAuraOn(enchantedCreature);

        Boomerang boomerang = new Boomerang();
        harness.setHand(player2, List.of(boomerang));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, originalTarget.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(originalTarget.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(enchantedCreature.getId()));
    }

    @Test
    @DisplayName("A spell with multiple targets is not redirected")
    void doesNotRedirectMultipleTargetSpell() {
        Permanent enchantedCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player1, new GrizzlyBears());
        castAuraOn(enchantedCreature);

        PeelFromReality peel = new PeelFromReality();
        harness.setHand(player2, List.of(peel));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, List.of(otherCreature.getId(), opponentCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(enchantedCreature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(otherCreature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(opponentCreature.getId()));
    }

    @Test
    @DisplayName("A spell that cannot target the enchanted creature keeps its target")
    void keepsTargetWhenEnchantedCreatureIsIllegal() {
        Permanent enchantedCreature = addCreatureReady(player2, new GrizzlyBears());
        castAuraOn(enchantedCreature);

        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player2, List.of(lavaAxe));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(enchantedCreature.getId()));
    }

    private void castAuraOn(Permanent target) {
        harness.setHand(player1, List.of(new CapturedByTheConsulate()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
