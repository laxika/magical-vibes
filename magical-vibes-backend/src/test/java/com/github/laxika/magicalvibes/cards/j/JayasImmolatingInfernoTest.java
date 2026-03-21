package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToEachTargetEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JayasImmolatingInfernoTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has correct targeting and effect")
    void hasCorrectProperties() {
        JayasImmolatingInferno card = new JayasImmolatingInferno();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getMinTargets()).isEqualTo(1);
        assertThat(card.getMaxTargets()).isEqualTo(3);
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(DealXDamageToEachTargetEffect.class);
    }

    // ===== Legendary sorcery restriction =====

    @Test
    @DisplayName("Cannot cast without controlling a legendary creature or planeswalker")
    void cannotCastWithoutLegendaryPermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // non-legendary
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new JayasImmolatingInferno()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 3, List.of(targetId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Can cast when controlling a legendary creature")
    void canCastWithLegendaryCreature() {
        harness.addToBattlefield(player1, new ArvadTheCursed()); // legendary creature
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new JayasImmolatingInferno()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castSorcery(player1, 0, 3, List.of(targetId));

        assertThat(harness.getGameData().stack).hasSize(1);
        assertThat(harness.getGameData().stack.getFirst().getCard().getName()).isEqualTo("Jaya's Immolating Inferno");
    }

    // ===== Damage to creatures =====

    @Test
    @DisplayName("Deals X damage to each of three creature targets")
    void dealsXDamageToThreeCreatures() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        // GrizzlyBears 2/2, GiantSpider 2/4, AirElemental 4/4
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new JayasImmolatingInferno()));
        harness.addMana(player1, ManaColor.RED, 5); // X=3, cost {3}{R}{R}

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player2.getId());
        UUID bearsId = bf.get(0).getId();
        UUID spiderId = bf.get(1).getId();
        UUID elementalId = bf.get(2).getId();

        harness.castSorcery(player1, 0, 3, List.of(bearsId, spiderId, elementalId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // GrizzlyBears took 3 damage (dies: 3 >= 2 toughness)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // GiantSpider took 3 damage (survives: 3 < 4 toughness)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Giant Spider"));

        // AirElemental took 3 damage (survives: 3 < 4 toughness)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Air Elemental"));
    }

    @Test
    @DisplayName("Deals X damage to a single target")
    void dealsXDamageToSingleTarget() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new JayasImmolatingInferno()));
        harness.addMana(player1, ManaColor.RED, 4); // X=2, cost {2}{R}{R}

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castSorcery(player1, 0, 2, List.of(bearsId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // GrizzlyBears took 2 damage (dies: 2 >= 2 toughness)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Damage to players =====

    @Test
    @DisplayName("Deals X damage to player targets")
    void dealsXDamageToPlayers() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new JayasImmolatingInferno()));
        harness.addMana(player1, ManaColor.RED, 6); // X=4, cost {4}{R}{R}

        harness.castSorcery(player1, 0, 4, List.of(player2.getId()));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Deals X damage to mix of creatures and players")
    void dealsXDamageToMixedTargets() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new JayasImmolatingInferno()));
        harness.addMana(player1, ManaColor.RED, 5); // X=3, cost {3}{R}{R}

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castSorcery(player1, 0, 3, List.of(bearsId, player2.getId()));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // GrizzlyBears took 3 damage (dies)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Player 2 took 3 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    // ===== Partial resolution =====

    @Test
    @DisplayName("Partially resolves when one creature target is removed before resolution")
    void partiallyResolvesWhenTargetRemoved() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new JayasImmolatingInferno()));
        harness.addMana(player1, ManaColor.RED, 5); // X=3

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID spiderId = harness.getPermanentId(player2, "Giant Spider");

        harness.castSorcery(player1, 0, 3, List.of(bearsId, spiderId, player2.getId()));

        // Remove bears before resolution
        harness.getGameData().playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Bears was removed — skipped
        // GiantSpider took 3 damage (survives: 3 < 4 toughness)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Giant Spider"));
        // Player 2 took 3 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    // ===== Stack and graveyard =====

    @Test
    @DisplayName("Goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new JayasImmolatingInferno()));
        harness.addMana(player1, ManaColor.RED, 4); // X=2

        harness.castSorcery(player1, 0, 2, List.of(player2.getId()));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Jaya's Immolating Inferno"));
    }
}
