package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SeismicShiftTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target land and makes two creatures unable to block")
    void destroysLandAndMakesTwoCreaturesCantBlock() {
        harness.addToBattlefield(player2, new Mountain());
        GrizzlyBears bear1 = new GrizzlyBears();
        GrizzlyBears bear2 = new GrizzlyBears();
        harness.addToBattlefield(player2, bear1);
        harness.addToBattlefield(player2, bear2);
        harness.setHand(player1, List.of(new SeismicShift()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID landId = harness.getPermanentId(player2, "Mountain");
        List<Permanent> bf = gd.playerBattlefields.get(player2.getId());
        UUID creature1Id = bf.stream().filter(p -> p.getCard() == bear1).findFirst().get().getId();
        UUID creature2Id = bf.stream().filter(p -> p.getCard() == bear2).findFirst().get().getId();

        harness.castSorcery(player1, 0, List.of(landId, creature1Id, creature2Id));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");

        bf = gd.playerBattlefields.get(player2.getId());
        assertThat(bf).hasSize(2);
        assertThat(bf.get(0).isCantBlockThisTurn()).isTrue();
        assertThat(bf.get(1).isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Can cast with only the land target (zero creatures)")
    void canCastWithOnlyLandTarget() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new SeismicShift()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID landId = harness.getPermanentId(player2, "Mountain");
        harness.castSorcery(player1, 0, List.of(landId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
    }

    @Test
    @DisplayName("Can cast with land and one creature target")
    void canCastWithLandAndOneCreature() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SeismicShift()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID landId = harness.getPermanentId(player2, "Mountain");
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, List.of(landId, creatureId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");

        Permanent bear = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bear.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Creatures still can't block even if land target is removed before resolution")
    void creaturesCantBlockEvenIfLandTargetRemoved() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SeismicShift()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID landId = harness.getPermanentId(player2, "Mountain");
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, List.of(landId, creatureId));

        // Remove the land before resolution
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getCard().getName().equals("Mountain"));

        harness.passBothPriorities();

        Permanent bear = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bear.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Spell goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new SeismicShift()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID landId = harness.getPermanentId(player2, "Mountain");
        harness.castSorcery(player1, 0, List.of(landId));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Seismic Shift"));
    }
}
