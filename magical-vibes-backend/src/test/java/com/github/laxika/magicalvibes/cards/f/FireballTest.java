package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DealXDamageDividedEvenlyAmongTargetsEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FireballTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Fireball has correct card properties")
    void hasCorrectProperties() {
        Fireball card = new Fireball();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getMinTargets()).isEqualTo(1);
        assertThat(card.getMaxTargets()).isEqualTo(99);
        assertThat(card.getAdditionalCostPerExtraTarget()).isEqualTo(1);
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(DealXDamageDividedEvenlyAmongTargetsEffect.class);
    }

    // ===== Single target =====

    @Test
    @DisplayName("X=5 with 1 target deals 5 damage to that target")
    void singleTargetDealsFullXDamage() {
        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, 6); // {5}{R}
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 5, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("X=4 with 1 creature target deals 4 damage and kills a 2/2")
    void singleTargetKillsCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, 5); // {4}{R}

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, 4, List.of(bearsId));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("X=0 with 1 target deals 0 damage")
    void xZeroDealsNoDamage() {
        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, 1); // {0}{R}
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 0, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Multiple targets — even division =====

    @Test
    @DisplayName("X=6 with 2 targets deals 3 damage to each")
    void twoTargetsEvenDivision() {
        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, 8); // {6}{R} + {1} for extra target = 8 total
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 6, List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("X=6 with 3 targets deals 2 damage to each")
    void threeTargetsEvenDivision() {
        harness.addToBattlefield(player2, new GiantSpider());  // 2/4
        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, 9); // {6}{R} + {2} for 2 extra targets = 9 total
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        UUID spiderId = harness.getPermanentId(player2, "Giant Spider");
        harness.castSorcery(player1, 0, 6, List.of(player1.getId(), player2.getId(), spiderId));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        // Giant Spider (2/4) survives 2 damage
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Giant Spider"));
    }

    // ===== Rounding down =====

    @Test
    @DisplayName("X=5 with 2 targets deals 2 damage to each (rounded down)")
    void twoTargetsRoundsDown() {
        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, 7); // {5}{R} + {1} = 7 total
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 5, List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();

        // floor(5/2) = 2 damage each
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("X=5 with 3 targets deals 1 damage to each (rounded down)")
    void threeTargetsRoundsDown() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, 8); // {5}{R} + {2} = 8 total
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, 5, List.of(player1.getId(), player2.getId(), bearsId));
        harness.passBothPriorities();

        // floor(5/3) = 1 damage each
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        // GrizzlyBears (2/2) survives 1 damage
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Additional cost per target =====

    @Test
    @DisplayName("Casting with 2 targets costs {1} more than with 1 target")
    void additionalCostPerTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new Fireball()));
        // X=4, 2 targets: cost = {4}{R} + {1} = 6 total. Give exactly 6 mana.
        harness.addMana(player1, ManaColor.RED, 6);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID spiderId = harness.getPermanentId(player2, "Giant Spider");

        harness.castSorcery(player1, 0, 4, List.of(bearsId, spiderId));
        harness.passBothPriorities();

        // floor(4/2) = 2 damage each
        // GrizzlyBears (2/2) dies, GiantSpider (2/4) survives
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Giant Spider"));
    }

    @Test
    @DisplayName("Cannot cast with 2 targets if not enough mana for additional cost")
    void cannotCastWithTwoTargetsIfNotEnoughMana() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new Fireball()));
        // X=4, 2 targets: need {4}{R} + {1} = 6, but only give 5
        harness.addMana(player1, ManaColor.RED, 5);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID spiderId = harness.getPermanentId(player2, "Giant Spider");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 4, List.of(bearsId, spiderId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting with 3 targets costs {2} more than with 1 target")
    void additionalCostForThreeTargets() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new Fireball()));
        // X=3, 3 targets: cost = {3}{R} + {2} = 6 total. Give exactly 6 mana.
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setLife(player2, 20);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID spiderId = harness.getPermanentId(player2, "Giant Spider");

        harness.castSorcery(player1, 0, 3, List.of(bearsId, spiderId, player2.getId()));
        harness.passBothPriorities();

        // floor(3/3) = 1 damage each
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        // Both creatures survive 1 damage
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Giant Spider"));
    }

    // ===== Stack and graveyard =====

    @Test
    @DisplayName("Fireball puts correct entry on the stack")
    void putsCorrectEntryOnStack() {
        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 3, List.of(player2.getId()));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Fireball");
        assertThat(entry.getXValue()).isEqualTo(3);
        assertThat(entry.getTargetIds()).containsExactly(player2.getId());
    }

    @Test
    @DisplayName("Fireball goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 3, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Fireball"));
    }

    // ===== Target removed before resolution =====

    @Test
    @DisplayName("Deals damage to surviving targets when one creature target is removed")
    void partiallyResolvesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, 6); // X=4, 2 targets: {4}{R}+{1}
        harness.setLife(player2, 20);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castSorcery(player1, 0, 4, List.of(bearsId, player2.getId()));

        // Remove the creature before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        // Bears was removed — skipped. Player 2 still takes floor(4/2)=2 damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Mixed targets (creatures + players) =====

    @Test
    @DisplayName("Deals evenly divided damage to mix of creatures and players")
    void dealsDamageToMixOfTargets() {
        harness.addToBattlefield(player2, new GrizzlyBears());  // 2/2
        harness.addToBattlefield(player2, new AirElemental());  // 4/4
        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, 9); // X=6, 3 targets: {6}{R}+{2}
        harness.setLife(player2, 20);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID elementalId = harness.getPermanentId(player2, "Air Elemental");

        harness.castSorcery(player1, 0, 6, List.of(bearsId, elementalId, player2.getId()));
        harness.passBothPriorities();

        // floor(6/3) = 2 damage each
        // GrizzlyBears (2/2) dies
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // AirElemental (4/4) survives 2 damage
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Air Elemental"));
        // Player 2 takes 2 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
