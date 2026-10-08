package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.p.PhyrexianUnlife;
import com.github.laxika.magicalvibes.cards.p.PorcelainLegionnaire;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VaporSnag.class, PorcelainLegionnaire.class, PhyrexianUnlife.class, Island.class})
class VaporSnagTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Vapor Snag puts it on the stack with target")
    void castingPutsOnStack() {
        harness.addToBattlefield(player2, new PorcelainLegionnaire());
        harness.setHand(player1, List.of(new VaporSnag()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Porcelain Legionnaire");
        harness.castInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        harness.addToBattlefield(player1, new PorcelainLegionnaire()); // valid target so spell is playable
        harness.addToBattlefield(player2, new PhyrexianUnlife());
        harness.setHand(player1, List.of(new VaporSnag()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Phyrexian Unlife");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new PorcelainLegionnaire()); // valid target so spell is playable
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new VaporSnag()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Island");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Resolving returns target creature to owner's hand and its controller loses 1 life")
    void resolvingReturnsCreatureAndControllerLosesLife() {
        harness.addToBattlefield(player2, new PorcelainLegionnaire());
        harness.setHand(player1, List.of(new VaporSnag()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Porcelain Legionnaire");
        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Porcelain Legionnaire");
        harness.assertInHand(player2, "Porcelain Legionnaire");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Bouncing own creature causes self to lose 1 life")
    void bouncingOwnCreatureCausesSelfLifeLoss() {
        harness.addToBattlefield(player1, new PorcelainLegionnaire());
        harness.setHand(player1, List.of(new VaporSnag()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        UUID targetId = harness.getPermanentId(player1, "Porcelain Legionnaire");
        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Porcelain Legionnaire");
        harness.assertInHand(player1, "Porcelain Legionnaire");
        // Caster is also the creature's controller, so they lose 1 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Vapor Snag goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new PorcelainLegionnaire());
        harness.setHand(player1, List.of(new VaporSnag()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Porcelain Legionnaire");
        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Vapor Snag");
    }

    @Test
    @DisplayName("Fizzles if target is removed before resolution — no life loss")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new PorcelainLegionnaire());
        harness.setHand(player1, List.of(new VaporSnag()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Porcelain Legionnaire");
        harness.castInstant(player1, 0, targetId);

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        // No life loss when spell fizzles
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Returns a stolen creature to its owner while its controller loses life")
    void differentOwnerAndController() {
        PorcelainLegionnaire creature = new PorcelainLegionnaire();
        creature.setOwnerId(player1.getId());
        var permanent = harness.addToBattlefieldAndReturn(player2, creature);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new VaporSnag()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, permanent.getId());

        harness.assertNotOnBattlefield(player2, "Porcelain Legionnaire");
        harness.assertInHand(player1, "Porcelain Legionnaire");
        harness.assertNotInHand(player2, "Porcelain Legionnaire");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Uses the creature's controller at resolution after control changes")
    void controllerChangesBeforeResolution() {
        PorcelainLegionnaire creature = new PorcelainLegionnaire();
        creature.setOwnerId(player2.getId());
        var permanent = harness.addToBattlefieldAndReturn(player2, creature);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new VaporSnag()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, permanent.getId());

        harness.getGameData().playerBattlefields.get(player2.getId()).remove(permanent);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(permanent);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Porcelain Legionnaire");
        harness.assertInHand(player2, "Porcelain Legionnaire");
        harness.assertNotInHand(player1, "Porcelain Legionnaire");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Life loss remains life loss below zero with Phyrexian Unlife")
    void lifeLossIsNotDamage() {
        harness.addToBattlefield(player2, new PhyrexianUnlife());
        var permanent = harness.addToBattlefieldAndReturn(player2, new PorcelainLegionnaire());
        harness.setLife(player2, 0);
        harness.setHand(player1, List.of(new VaporSnag()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, permanent.getId());

        harness.assertInHand(player2, "Porcelain Legionnaire");
        harness.assertLife(player2, -1);
        assertThat(harness.getGameData().playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }
}
