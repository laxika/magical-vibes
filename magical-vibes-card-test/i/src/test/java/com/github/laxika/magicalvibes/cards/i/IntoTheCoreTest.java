package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HexplateGolem;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
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

@CardUsed({IntoTheCore.class, GoldMyr.class, GrizzlyBears.class, HexplateGolem.class, IchorWellspring.class})
class IntoTheCoreTest extends BaseCardTest {

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

        harness.castAndResolveInstant(player1, 0, List.of(id1, id2));

        // Both artifacts should be exiled, not on the battlefield
        harness.assertNotOnBattlefield(player2, "Gold Myr");
        harness.assertNotOnBattlefield(player2, "Hexplate Golem");

        // Both should be in exile
        assertThat(gd.getPlayerExiledCards(player2.getId()))
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

        harness.castAndResolveInstant(player1, 0, List.of(ownMyrId, oppGolemId));

        harness.assertNotOnBattlefield(player1, "Gold Myr");
        harness.assertNotOnBattlefield(player2, "Hexplate Golem");

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Gold Myr"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
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

        harness.castAndResolveInstant(player1, 0, List.of(wellspringId, myrId));

        harness.assertNotOnBattlefield(player2, "Ichor Wellspring");
        harness.assertNotOnBattlefield(player2, "Gold Myr");
    }

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
        harness.assertNotOnBattlefield(player2, "Hexplate Golem");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Hexplate Golem"));
    }

    @Test
    @DisplayName("Stack is empty after resolution")
    void stackIsEmptyAfterResolution() {
        harness.addToBattlefield(player2, new GoldMyr());
        harness.addToBattlefield(player2, new HexplateGolem());
        harness.setHand(player1, List.of(new IntoTheCore()));
        harness.addMana(player1, ManaColor.RED, 4);

        List<Permanent> bf = gd.playerBattlefields.get(player2.getId());
        harness.castAndResolveInstant(player1, 0, List.of(bf.get(0).getId(), bf.get(1).getId()));

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
        harness.castAndResolveInstant(player1, 0, List.of(bf.get(0).getId(), bf.get(1).getId()));

        harness.assertInGraveyard(player1, "Into the Core");
    }

    @Test
    @DisplayName("Cannot cast with more than two artifact targets")
    void cannotCastWithMoreThanTwoTargets() {
        harness.addToBattlefield(player2, new HexplateGolem());
        harness.addToBattlefield(player2, new HexplateGolem());
        harness.addToBattlefield(player2, new HexplateGolem());
        harness.setHand(player1, List.of(new IntoTheCore()));
        harness.addMana(player1, ManaColor.RED, 4);

        List<UUID> targets = gd.playerBattlefields.get(player2.getId()).stream()
                .map(Permanent::getId).toList();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targets))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between");
    }

    @Test
    @DisplayName("Does not resolve when both targets leave the battlefield")
    void doesNotResolveWhenBothTargetsLeave() {
        harness.addToBattlefield(player2, new HexplateGolem());
        harness.addToBattlefield(player2, new HexplateGolem());
        harness.setHand(player1, List.of(new IntoTheCore()));
        harness.addMana(player1, ManaColor.RED, 4);

        List<Permanent> targets = List.copyOf(gd.playerBattlefields.get(player2.getId()));
        harness.castInstant(player1, 0, targets.stream().map(Permanent::getId).toList());
        harness.inMutationScope(() -> targets.forEach(target ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target)));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Into the Core");
    }

    @Test
    @DisplayName("Exiles the first target when only the second target leaves")
    void exilesFirstTargetWhenSecondTargetLeaves() {
        harness.addToBattlefield(player2, new HexplateGolem());
        harness.addToBattlefield(player2, new HexplateGolem());
        harness.setHand(player1, List.of(new IntoTheCore()));
        harness.addMana(player1, ManaColor.RED, 4);

        List<Permanent> targets = List.copyOf(gd.playerBattlefields.get(player2.getId()));
        harness.castInstant(player1, 0, targets.stream().map(Permanent::getId).toList());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, targets.get(1)));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getId()).containsExactly(targets.getFirst().getCard().getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getId()).containsExactly(targets.get(1).getCard().getId());
        harness.assertInGraveyard(player1, "Into the Core");
    }
}
