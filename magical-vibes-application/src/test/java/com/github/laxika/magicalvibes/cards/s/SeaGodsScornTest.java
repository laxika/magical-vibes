package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeaGodsScorn.class, AngelicChorus.class, GrizzlyBears.class, Island.class})
class SeaGodsScornTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to three target creatures and enchantments to their owners' hands")
    void returnsThreeMixedTargets() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new AngelicChorus());

        cast(List.of(ownCreature.getId(), opponentCreature.getId(), enchantment.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Angelic Chorus");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can be cast with fewer than three targets")
    void returnsOneTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new AngelicChorus());

        cast(List.of(target.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof AngelicChorus)
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can be cast with no targets")
    void castsWithNoTargets() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(List.of());

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(target);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new SeaGodsScorn()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creatures or enchantments");
    }

    @Test
    @DisplayName("Can return exactly two enchantments")
    void returnsTwoEnchantments() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AngelicChorus());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());

        cast(List.of(first.getId(), second.getId()));

        harness.assertNotOnBattlefield(player1, "Angelic Chorus");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInHand(player1, "Angelic Chorus");
        harness.assertInHand(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Cannot choose more than three targets")
    void cannotChooseFourTargets() {
        List<java.util.UUID> targets = java.util.stream.IntStream.range(0, 4)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId())
                .toList();
        harness.setHand(player1, List.of(new SeaGodsScorn()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targets))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose the same permanent twice")
    void cannotChooseDuplicateTargets() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SeaGodsScorn()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returns remaining legal targets when one target leaves before resolution")
    void resolvesWithOneTargetMissing() {
        Permanent removed = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new SeaGodsScorn()));
        addMana();
        harness.castSorcery(player1, 0, List.of(removed.getId(), remaining.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, removed));

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Angelic Chorus");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player1, "Sea God's Scorn");
    }

    @Test
    @DisplayName("Does not resolve when every chosen target has left the battlefield")
    void allTargetsMissing() {
        Permanent removed = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SeaGodsScorn()));
        addMana();
        harness.castSorcery(player1, 0, List.of(removed.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, removed));

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Sea God's Scorn");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returns a permanent to its owner rather than its current controller")
    void returnsToOwnerHand() {
        GrizzlyBears creature = new GrizzlyBears();
        creature.setOwnerId(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, creature);

        cast(List.of(target.getId()));

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    private void cast(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new SeaGodsScorn()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, targetIds);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
