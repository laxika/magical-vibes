package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HexplateGolem;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IntoTheCoreTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Into the Core has correct card properties")
    void hasCorrectProperties() {
        IntoTheCore card = new IntoTheCore();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getMinTargets()).isEqualTo(2);
        assertThat(card.getMaxTargets()).isEqualTo(2);
        assertThat(card.getTargetFilter()).isNotNull();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(ExileTargetPermanentEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting with two artifact targets puts it on the stack")
    void castingWithTwoArtifactTargetsPutsOnStack() {
        harness.addToBattlefield(player2, new GoldMyr());
        harness.addToBattlefield(player2, new HexplateGolem());
        harness.setHand(player1, List.of(new IntoTheCore()));
        harness.addMana(player1, ManaColor.RED, 4);

        List<Permanent> bf = gd.playerBattlefields.get(player2.getId());
        UUID id1 = bf.get(0).getId();
        UUID id2 = bf.get(1).getId();

        harness.castInstant(player1, 0, List.of(id1, id2));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Into the Core");
        assertThat(entry.getTargetIds()).containsExactly(id1, id2);
    }

    @Test
    @DisplayName("Cannot cast with fewer than 2 targets")
    void cannotCastWithFewerThanTwoTargets() {
        harness.addToBattlefield(player2, new GoldMyr());
        harness.setHand(player1, List.of(new IntoTheCore()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID id1 = gd.playerBattlefields.get(player2.getId()).get(0).getId();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(id1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between");
    }

    @Test
    @DisplayName("Cannot cast with duplicate targets")
    void cannotCastWithDuplicateTargets() {
        harness.addToBattlefield(player2, new GoldMyr());
        harness.setHand(player1, List.of(new IntoTheCore()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID id1 = gd.playerBattlefields.get(player2.getId()).get(0).getId();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(id1, id1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("All targets must be different");
    }

    @Test
    @DisplayName("Cannot target non-artifact permanents")
    void cannotTargetNonArtifactPermanents() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GoldMyr());
        harness.setHand(player1, List.of(new IntoTheCore()));
        harness.addMana(player1, ManaColor.RED, 4);

        List<Permanent> bf = gd.playerBattlefields.get(player2.getId());
        UUID bearsId = bf.get(0).getId();
        UUID myrId = bf.get(1).getId();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bearsId, myrId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.addToBattlefield(player2, new GoldMyr());
        harness.addToBattlefield(player2, new HexplateGolem());
        harness.setHand(player1, List.of(new IntoTheCore()));
        harness.addMana(player1, ManaColor.RED, 2);

        List<Permanent> bf = gd.playerBattlefields.get(player2.getId());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bf.get(0).getId(), bf.get(1).getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Exiles both targeted artifacts on resolution")
    void exilesBothTargetedArtifacts() {
        harness.addToBattlefield(player2, new GoldMyr());
        harness.addToBattlefield(player2, new HexplateGolem());
        harness.setHand(player1, List.of(new IntoTheCore()));
        harness.addMana(player1, ManaColor.RED, 4);

        List<Permanent> bf = gd.playerBattlefields.get(player2.getId());
        UUID id1 = bf.get(0).getId();
        UUID id2 = bf.get(1).getId();

        harness.castInstant(player1, 0, List.of(id1, id2));
        harness.passBothPriorities();

        // Both artifacts should be exiled, not on the battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Gold Myr"))
                .noneMatch(p -> p.getCard().getName().equals("Hexplate Golem"));

        // Both should be in exile
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Gold Myr"))
                .anyMatch(c -> c.getName().equals("Hexplate Golem"));
    }

    @Test
    @DisplayName("Can target own artifacts")
    void canTargetOwnArtifacts() {
        harness.addToBattlefield(player1, new GoldMyr());
        harness.addToBattlefield(player2, new HexplateGolem());
        harness.setHand(player1, List.of(new IntoTheCore()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID ownMyrId = harness.getPermanentId(player1, "Gold Myr");
        UUID oppGolemId = harness.getPermanentId(player2, "Hexplate Golem");

        harness.castInstant(player1, 0, List.of(ownMyrId, oppGolemId));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Gold Myr"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Hexplate Golem"));

        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Gold Myr"));
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Hexplate Golem"));
    }

    @Test
    @DisplayName("Can target non-creature artifacts")
    void canTargetNonCreatureArtifacts() {
        harness.addToBattlefield(player2, new IchorWellspring());
        harness.addToBattlefield(player2, new GoldMyr());
        harness.setHand(player1, List.of(new IntoTheCore()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID wellspringId = harness.getPermanentId(player2, "Ichor Wellspring");
        UUID myrId = harness.getPermanentId(player2, "Gold Myr");

        harness.castInstant(player1, 0, List.of(wellspringId, myrId));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Ichor Wellspring"))
                .noneMatch(p -> p.getCard().getName().equals("Gold Myr"));
    }

    // ===== Partial resolution =====

    @Test
    @DisplayName("Partially resolves when one target is removed before resolution")
    void partiallyResolvesWhenOneTargetRemoved() {
        harness.addToBattlefield(player2, new GoldMyr());
        harness.addToBattlefield(player2, new HexplateGolem());
        harness.setHand(player1, List.of(new IntoTheCore()));
        harness.addMana(player1, ManaColor.RED, 4);

        List<Permanent> bf = gd.playerBattlefields.get(player2.getId());
        UUID myrId = bf.get(0).getId();
        UUID golemId = bf.get(1).getId();

        harness.castInstant(player1, 0, List.of(myrId, golemId));

        // Remove the first target (Gold Myr) before resolution
        gd.playerBattlefields.get(player2.getId()).removeFirst();

        harness.passBothPriorities();

        // Gold Myr was removed — skipped
        // Hexplate Golem should be exiled
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Hexplate Golem"));
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Hexplate Golem"));
    }

    // ===== Stack and graveyard =====

    @Test
    @DisplayName("Stack is empty after resolution")
    void stackIsEmptyAfterResolution() {
        harness.addToBattlefield(player2, new GoldMyr());
        harness.addToBattlefield(player2, new HexplateGolem());
        harness.setHand(player1, List.of(new IntoTheCore()));
        harness.addMana(player1, ManaColor.RED, 4);

        List<Permanent> bf = gd.playerBattlefields.get(player2.getId());
        harness.castInstant(player1, 0, List.of(bf.get(0).getId(), bf.get(1).getId()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Into the Core goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.addToBattlefield(player2, new GoldMyr());
        harness.addToBattlefield(player2, new HexplateGolem());
        harness.setHand(player1, List.of(new IntoTheCore()));
        harness.addMana(player1, ManaColor.RED, 4);

        List<Permanent> bf = gd.playerBattlefields.get(player2.getId());
        harness.castInstant(player1, 0, List.of(bf.get(0).getId(), bf.get(1).getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Into the Core"));
    }
}
