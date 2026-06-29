package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageToAnyTargetsEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BogardanHellkiteTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Bogardan Hellkite has ON_ENTER_BATTLEFIELD DealDividedDamageToAnyTargetsEffect(5, 5)")
    void hasETBEffect() {
        BogardanHellkite card = new BogardanHellkite();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(DealDividedDamageToAnyTargetsEffect.class);
        DealDividedDamageToAnyTargetsEffect effect =
                (DealDividedDamageToAnyTargetsEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.totalDamage()).isEqualTo(5);
        assertThat(effect.maxTargets()).isEqualTo(5);
    }

    // ===== ETB trigger: deal 5 divided damage =====

    @Nested
    @DisplayName("ETB trigger")
    class ETBTrigger {

        @Test
        @DisplayName("ETB deals all 5 damage to a single player")
        void etbDeals5DamageToSinglePlayer() {
            harness.setLife(player2, 20);

            gd.pendingETBDamageAssignments = Map.of(player2.getId(), 5);

            castBogardanHellkite();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        }

        @Test
        @DisplayName("ETB deals all 5 damage to a single creature, killing it")
        void etbDeals5DamageToSingleCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

            gd.pendingETBDamageAssignments = Map.of(bearsId, 5);

            castBogardanHellkite();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("ETB divides damage among a creature and a player")
        void etbDividesDamageAmongCreatureAndPlayer() {
            harness.setLife(player2, 20);
            harness.addToBattlefield(player2, new GrizzlyBears());
            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

            gd.pendingETBDamageAssignments = Map.of(bearsId, 2, player2.getId(), 3);

            castBogardanHellkite();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            // Bears took 2 damage — lethal for 2/2
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));

            // Player took 3 damage
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        }

        @Test
        @DisplayName("ETB divides damage among both players and a creature")
        void etbDividesDamageAmongThreeTargets() {
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);
            harness.addToBattlefield(player2, new GrizzlyBears());
            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

            gd.pendingETBDamageAssignments = Map.of(
                    bearsId, 1,
                    player1.getId(), 2,
                    player2.getId(), 2
            );

            castBogardanHellkite();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                    .findFirst().orElse(null);
            assertThat(bears).isNotNull();
            assertThat(bears.getMarkedDamage()).isEqualTo(1);

            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        }

        @Test
        @DisplayName("ETB with no damage assignments does nothing")
        void etbWithNoDamageAssignments() {
            harness.setLife(player2, 20);

            gd.pendingETBDamageAssignments = Map.of();

            castBogardanHellkite();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        }
    }

    // ===== Helpers =====

    private void castBogardanHellkite() {
        harness.setHand(player1, List.of(new BogardanHellkite()));
        harness.addMana(player1, ManaColor.RED, 8);
        harness.castCreature(player1, 0);
    }
}
