package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DealOrderedDamageToAnyTargetsEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConeOfFlameTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Cone of Flame has correct card properties")
    void hasCorrectProperties() {
        ConeOfFlame card = new ConeOfFlame();

        assertThat(card.getName()).isEqualTo("Cone of Flame");
        assertThat(card.getType()).isEqualTo(CardType.SORCERY);
        assertThat(card.getManaCost()).isEqualTo("{3}{R}{R}");
        assertThat(card.getColor()).isEqualTo(CardColor.RED);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getMinTargets()).isEqualTo(3);
        assertThat(card.getMaxTargets()).isEqualTo(3);
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(DealOrderedDamageToAnyTargetsEffect.class);
        DealOrderedDamageToAnyTargetsEffect effect = (DealOrderedDamageToAnyTargetsEffect) card.getEffects(EffectSlot.SPELL).get(0);
        assertThat(effect.damageAmounts()).containsExactly(1, 2, 3);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Cone of Flame with 3 creature targets puts it on the stack")
    void castingWithThreeCreatureTargetsPutsOnStack() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new ConeOfFlame()));
        harness.addMana(player1, ManaColor.RED, 5);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player2.getId());
        UUID id1 = bf.get(0).getId();
        UUID id2 = bf.get(1).getId();
        UUID id3 = bf.get(2).getId();

        harness.castSorcery(player1, 0, List.of(id1, id2, id3));

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Cone of Flame");
        assertThat(entry.getTargetPermanentIds()).containsExactly(id1, id2, id3);
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new ConeOfFlame()));
        harness.addMana(player1, ManaColor.RED, 3);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player2.getId());

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(bf.get(0).getId(), bf.get(1).getId(), bf.get(2).getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast with fewer than 3 targets")
    void cannotCastWithFewerThanThreeTargets() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new ConeOfFlame()));
        harness.addMana(player1, ManaColor.RED, 5);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player2.getId());

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(bf.get(0).getId(), bf.get(1).getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between");
    }

    @Test
    @DisplayName("Cannot cast with duplicate targets")
    void cannotCastWithDuplicateTargets() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new ConeOfFlame()));
        harness.addMana(player1, ManaColor.RED, 5);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player2.getId());
        UUID id1 = bf.get(0).getId();
        UUID id2 = bf.get(1).getId();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(id1, id2, id1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("All targets must be different");
    }

    // ===== Damage to creatures =====

    @Test
    @DisplayName("Deals ordered damage: 1 to first, 2 to second, 3 to third creature")
    void dealsOrderedDamageToCreatures() {
        // GrizzlyBears (2/2): 1 damage → survives, 2 damage → dies, 3 damage → dies
        // GiantSpider (2/4): survives 1, 2, or 3 damage
        // AirElemental (4/4): survives 1, 2, or 3 damage
        harness.addToBattlefield(player2, new GiantSpider());    // Target 1: 1 damage (survives, 4 toughness)
        harness.addToBattlefield(player2, new GrizzlyBears());   // Target 2: 2 damage (dies, 2 toughness)
        harness.addToBattlefield(player2, new AirElemental());   // Target 3: 3 damage (survives, 4 toughness)
        harness.setHand(player1, List.of(new ConeOfFlame()));
        harness.addMana(player1, ManaColor.RED, 5);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player2.getId());
        UUID spiderId = bf.get(0).getId();
        UUID bearsId = bf.get(1).getId();
        UUID elementalId = bf.get(2).getId();

        harness.castSorcery(player1, 0, List.of(spiderId, bearsId, elementalId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // GiantSpider took 1 damage (survives: 1 < 4 toughness)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Giant Spider"));

        // GrizzlyBears took 2 damage (dies: 2 >= 2 toughness)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // AirElemental took 3 damage (survives: 3 < 4 toughness)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Air Elemental"));
    }

    @Test
    @DisplayName("3 damage to third target kills a 2/2 creature")
    void thirdTargetThreeDamageKillsSmallCreature() {
        harness.addToBattlefield(player2, new AirElemental());   // Target 1: 1 damage (survives)
        harness.addToBattlefield(player2, new GiantSpider());    // Target 2: 2 damage (survives)
        harness.addToBattlefield(player2, new GrizzlyBears());   // Target 3: 3 damage (dies)
        harness.setHand(player1, List.of(new ConeOfFlame()));
        harness.addMana(player1, ManaColor.RED, 5);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player2.getId());
        harness.castSorcery(player1, 0, List.of(bf.get(0).getId(), bf.get(1).getId(), bf.get(2).getId()));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Air Elemental"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Giant Spider"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Damage to players =====

    @Test
    @DisplayName("Deals ordered damage to three players/self targets")
    void dealsOrderedDamageToPlayers() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ConeOfFlame()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        // Target 1 (1 dmg): player2, Target 2 (2 dmg): creature, Target 3 (3 dmg): player1
        harness.castSorcery(player1, 0, List.of(player2.getId(), bearsId, player1.getId()));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Player 2 took 1 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        // Player 1 took 3 damage
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        // GrizzlyBears took 2 damage (dies: 2 >= 2)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Can target both players and a creature")
    void canTargetBothPlayersAndCreature() {
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ConeOfFlame()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID spiderId = harness.getPermanentId(player2, "Giant Spider");

        // Target 1 (1 dmg): player1, Target 2 (2 dmg): player2, Target 3 (3 dmg): creature
        harness.castSorcery(player1, 0, List.of(player1.getId(), player2.getId(), spiderId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        // GiantSpider took 3 damage (survives: 3 < 4)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Giant Spider"));
    }

    // ===== Targeting own creatures =====

    @Test
    @DisplayName("Can target own creatures")
    void canTargetOwnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GiantSpider());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new ConeOfFlame()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID ownBearsId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID ownSpiderId = harness.getPermanentId(player1, "Giant Spider");
        UUID oppElementalId = harness.getPermanentId(player2, "Air Elemental");

        harness.castSorcery(player1, 0, List.of(ownBearsId, ownSpiderId, oppElementalId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Own GrizzlyBears took 1 damage (survives: 1 < 2)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Own GiantSpider took 2 damage (survives: 2 < 4)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Giant Spider"));
        // Opponent AirElemental took 3 damage (survives: 3 < 4)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Air Elemental"));
    }

    // ===== Partial resolution =====

    @Test
    @DisplayName("Partially resolves when one creature target is removed")
    void partiallyResolvesWhenOneTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ConeOfFlame()));
        harness.addMana(player1, ManaColor.RED, 5);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player2.getId());
        UUID bearsId = bf.get(0).getId();
        UUID spiderId = bf.get(1).getId();

        // Targets: bears (1 dmg), spider (2 dmg), player2 (3 dmg)
        harness.castSorcery(player1, 0, List.of(bearsId, spiderId, player2.getId()));

        // Remove the first target (bears) before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).removeFirst();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Bears was removed before resolution — skipped
        // GiantSpider took 2 damage (survives: 2 < 4)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Giant Spider"));
        // Player 2 took 3 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Still deals damage to remaining targets when all creature targets removed")
    void stillDamagesPlayersWhenCreatureTargetsRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ConeOfFlame()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        // Targets: bears (1 dmg), player1 (2 dmg), player2 (3 dmg)
        harness.castSorcery(player1, 0, List.of(bearsId, player1.getId(), player2.getId()));

        // Remove the creature before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Bears gone, damage skipped
        // Player 1 took 2 damage
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        // Player 2 took 3 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    // ===== Stack and graveyard =====

    @Test
    @DisplayName("Stack is empty after resolution")
    void stackIsEmptyAfterResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new ConeOfFlame()));
        harness.addMana(player1, ManaColor.RED, 5);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player2.getId());
        harness.castSorcery(player1, 0, List.of(bf.get(0).getId(), bf.get(1).getId(), bf.get(2).getId()));
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Cone of Flame goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new ConeOfFlame()));
        harness.addMana(player1, ManaColor.RED, 5);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player2.getId());
        harness.castSorcery(player1, 0, List.of(bf.get(0).getId(), bf.get(1).getId(), bf.get(2).getId()));
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Cone of Flame"));
    }
}
