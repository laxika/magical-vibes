package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeismicShift.class, Mountain.class, GrizzlyBears.class, SylvanAwakening.class})
class SeismicShiftTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target land and makes two creatures unable to block")
    void destroysLandAndMakesTwoCreaturesCantBlock() {
        harness.addToBattlefield(player2, new Mountain());
        Permanent bear1 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent bear2 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SeismicShift()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID landId = harness.getPermanentId(player2, "Mountain");
        UUID creature1Id = bear1.getId();
        UUID creature2Id = bear2.getId();

        harness.castAndResolveSorcery(player1, 0, List.of(landId, creature1Id, creature2Id));

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");

        List<Permanent> bf = gd.playerBattlefields.get(player2.getId());
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
        harness.castAndResolveSorcery(player1, 0, List.of(landId));

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
        harness.castAndResolveSorcery(player1, 0, List.of(landId, creatureId));

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
        harness.castAndResolveSorcery(player1, 0, List.of(landId));

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Seismic Shift");
    }

    @Test
    @DisplayName("Destroys the land even if the creature target leaves before resolution")
    void destroysLandWhenCreatureTargetLeaves() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SeismicShift()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, List.of(land.getId(), creature.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Mountain");
        harness.assertInGraveyard(player1, "Seismic Shift");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only targeted creatures are prevented from blocking")
    void leavesUntargetedCreatureAbleToBlock() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SeismicShift()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, List.of(land.getId(), target.getId()));

        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(other.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("An indestructible creature land targeted only as a land remains able to block")
    void landTargetDoesNotReceiveCreatureBlockingRestriction() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new SylvanAwakening(), new SeismicShift()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, List.of(land.getId()));

        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(land.isCantBlockThisTurn()).isFalse();
    }
}
