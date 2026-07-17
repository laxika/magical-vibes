package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    class ExileGraveyardMode {

        @Test
        @DisplayName("Empties the targeted player's graveyard")
        void exilesGraveyard() {
            harness.setGraveyard(player2, List.of(new GrizzlyBears(), new HillGiant()));
            harness.setHand(player1, List.of(new JundCharm()));
            addBRG();

            harness.castInstant(player1, 0, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("Mode 1: Jund Charm deals 2 damage to each creature")
    class MassDamageMode {

        @Test
        @DisplayName("Kills 2/2 creatures on both sides, larger creatures survive")
        void damagesEachCreature() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.addToBattlefield(player2, new HillGiant());
            harness.setHand(player1, List.of(new JundCharm()));
            addBRG();

            int player2Life = gd.playerLifeTotals.get(player2.getId());

            harness.castInstant(player1, 0, 1, null);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"))
                    .anyMatch(p -> p.getCard().getName().equals("Hill Giant"));
            // Players take no damage.
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(player2Life);
        }
    }

    @Nested
    @DisplayName("Mode 2: Put two +1/+1 counters on target creature")
    class CountersMode {

        @Test
        @DisplayName("Adds two +1/+1 counters to the target")
        void addsCounters() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new JundCharm()));
            addBRG();

            UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.castInstant(player1, 0, 2, targetId);
            harness.passBothPriorities();

            Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
            assertThat(bear.getEffectivePower()).isEqualTo(4);
            assertThat(bear.getEffectiveToughness()).isEqualTo(4);
        }

        @Test
        @DisplayName("Cannot target a noncreature permanent")
        void cannotTargetNoncreature() {
            harness.addToBattlefield(player1, new FountainOfYouth());
            harness.setHand(player1, List.of(new JundCharm()));
            addBRG();

            UUID fountainId = harness.getPermanentId(player1, "Fountain of Youth");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, fountainId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
