package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChainedToTheRocksTest extends BaseCardTest {

    private void castAndResolve(UUID enchantTargetId, UUID exileTargetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ChainedToTheRocks()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, List.of(enchantTargetId, exileTargetId));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB exiles target creature an opponent controls")
    void etbExilesOpponentCreature() {
        harness.addToBattlefield(player1, new Mountain());
        Permanent mountain = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent creature = gd.playerBattlefields.get(player2.getId()).getFirst();

        castAndResolve(mountain.getId(), creature.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(creature.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Exiled creature returns when Chained to the Rocks leaves the battlefield")
    void exiledCreatureReturnsWhenAuraLeaves() {
        harness.addToBattlefield(player1, new Mountain());
        Permanent mountain = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent creature = gd.playerBattlefields.get(player2.getId()).getFirst();

        castAndResolve(mountain.getId(), creature.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID auraId = harness.getPermanentId(player1, "Chained to the Rocks");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, auraId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot enchant a non-Mountain")
    void cannotEnchantNonMountain() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent creature = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.setHand(player1, List.of(new ChainedToTheRocks()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0,
                List.of(forest.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot enchant an opponent's Mountain")
    void cannotEnchantOpponentsMountain() {
        harness.addToBattlefield(player2, new Mountain());
        Permanent mountain = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent creature = gd.playerBattlefields.get(player2.getId()).get(1);
        harness.setHand(player1, List.of(new ChainedToTheRocks()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0,
                List.of(mountain.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot exile a creature you control")
    void cannotExileOwnCreature() {
        harness.addToBattlefield(player1, new Mountain());
        Permanent mountain = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent creature = gd.playerBattlefields.get(player1.getId()).get(1);
        harness.setHand(player1, List.of(new ChainedToTheRocks()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0,
                List.of(mountain.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature an opponent controls");
    }
}
