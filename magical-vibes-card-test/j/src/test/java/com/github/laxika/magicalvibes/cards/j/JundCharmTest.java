package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.o.ObeliskOfJund;
import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.c.CourtArchers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JundCharm.class, CylianElf.class, CourtArchers.class, ObeliskOfJund.class})
class JundCharmTest extends BaseCardTest {

    // Mode indices: 0 = exile target player's graveyard, 1 = 2 damage to each creature,
    //               2 = two +1/+1 counters on target creature.

    private void addBRG() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    @Nested
    @DisplayName("Mode 0: Exile target player's graveyard")
    @CardUsed({JundCharm.class, CylianElf.class, CourtArchers.class})
    class ExileGraveyardMode {

        @Test
        @DisplayName("Empties the targeted player's graveyard")
        void exilesGraveyard() {
            harness.setGraveyard(player2, List.of(new CylianElf(), new CourtArchers()));
            harness.setHand(player1, List.of(new JundCharm()));
            addBRG();

            harness.castInstant(player1, 0, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("Mode 1: Jund Charm deals 2 damage to each creature")
    @CardUsed({JundCharm.class, CylianElf.class, CourtArchers.class})
    class MassDamageMode {

        @Test
        @DisplayName("Kills 2/2 creatures on both sides, larger creatures survive")
        void damagesEachCreature() {
            harness.addToBattlefield(player1, new CylianElf());
            harness.addToBattlefield(player2, new CylianElf());
            harness.addToBattlefield(player2, new CourtArchers());
            harness.setHand(player1, List.of(new JundCharm()));
            addBRG();

            int player2Life = gd.playerLifeTotals.get(player2.getId());

            harness.castInstant(player1, 0, 1, null);
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Cylian Elf");
            harness.assertNotOnBattlefield(player2, "Cylian Elf");
            harness.assertOnBattlefield(player2, "Court Archers");
            // Players take no damage.
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(player2Life);
        }
    }

    @Nested
    @DisplayName("Mode 2: Put two +1/+1 counters on target creature")
    @CardUsed({JundCharm.class, CylianElf.class, ObeliskOfJund.class})
    class CountersMode {

        @Test
        @DisplayName("Adds two +1/+1 counters to the target")
        void addsCounters() {
            harness.addToBattlefield(player1, new CylianElf());
            harness.setHand(player1, List.of(new JundCharm()));
            addBRG();

            UUID targetId = harness.getPermanentId(player1, "Cylian Elf");
            harness.castInstant(player1, 0, 2, targetId);
            harness.passBothPriorities();

            Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
            assertThat(bear.getEffectivePower()).isEqualTo(4);
            assertThat(bear.getEffectiveToughness()).isEqualTo(4);
        }

        @Test
        @DisplayName("Cannot target a noncreature permanent")
        void cannotTargetNoncreature() {
            harness.addToBattlefield(player1, new ObeliskOfJund());
            harness.setHand(player1, List.of(new JundCharm()));
            addBRG();

            UUID fountainId = harness.getPermanentId(player1, "Obelisk of Jund");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, fountainId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
    @Test
    @DisplayName("Can exile its controller's graveyard without affecting the opponent")
    void exilesOwnGraveyard() {
        CylianElf elf = new CylianElf();
        CourtArchers archers = new CourtArchers();
        harness.setGraveyard(player1, List.of(elf));
        harness.setGraveyard(player2, List.of(archers));
        harness.setHand(player1, List.of(new JundCharm()));
        addBRG();

        harness.castInstant(player1, 0, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName()).containsExactly("Jund Charm");
        assertThat(gd.findExiledCard(elf.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(archers);
    }

    @Test
    @DisplayName("Can target a player with an empty graveyard")
    void targetsEmptyGraveyard() {
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new JundCharm()));
        addBRG();

        harness.castInstant(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Jund Charm");
    }

    @Test
    @DisplayName("Can put counters on an opponent's creature")
    void addsCountersToOpposingCreature() {
        harness.addToBattlefield(player2, new CylianElf());
        harness.setHand(player1, List.of(new JundCharm()));
        addBRG();

        harness.castInstant(player1, 0, 2, harness.getPermanentId(player2, "Cylian Elf"));
        harness.passBothPriorities();

        Permanent elf = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(elf.getEffectivePower()).isEqualTo(4);
        assertThat(elf.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Counter mode does nothing when its target dies before resolution")
    void removedCounterTargetDoesNotChangeModes() {
        harness.addToBattlefield(player1, new CylianElf());
        harness.addToBattlefield(player2, new CourtArchers());
        harness.setHand(player1, List.of(new JundCharm(), new JundCharm()));
        addBRG();
        addBRG();

        harness.castInstant(player1, 0, 2, harness.getPermanentId(player1, "Cylian Elf"));
        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Cylian Elf");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Court Archers");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName()).containsExactlyInAnyOrder("Cylian Elf", "Jund Charm", "Jund Charm");
    }
}
