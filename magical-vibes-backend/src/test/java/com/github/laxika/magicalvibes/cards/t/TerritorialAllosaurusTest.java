package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TerritorialAllosaurusTest extends BaseCardTest {

    // ===== Cast without kicker =====

    @Nested
    @DisplayName("Cast without kicker")
    class WithoutKicker {

        @Test
        @DisplayName("Enters the battlefield as 5/5 with no ETB trigger")
        void entersWithNoTrigger() {
            harness.setHand(player1, List.of(new TerritorialAllosaurus()));
            harness.addMana(player1, ManaColor.GREEN, 2);
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            harness.castCreature(player1, 0);
            harness.passBothPriorities(); // resolve creature spell

            harness.assertOnBattlefield(player1, "Territorial Allosaurus");
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("No ETB trigger even with opponent creature on battlefield")
        void noTriggerEvenWithTarget() {
            addCreature(player2);
            harness.setHand(player1, List.of(new TerritorialAllosaurus()));
            harness.addMana(player1, ManaColor.GREEN, 2);
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            harness.castCreature(player1, 0);
            harness.passBothPriorities(); // resolve creature spell

            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        }
    }

    // ===== Cast with kicker =====

    @Nested
    @DisplayName("Cast with kicker")
    class WithKicker {

        @Test
        @DisplayName("ETB triggered ability goes on the stack when kicked")
        void etbTriggersOnStack() {
            Permanent target = addCreature(player2);
            castKicked(target.getId());

            harness.assertOnBattlefield(player1, "Territorial Allosaurus");
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        }

        @Test
        @DisplayName("Fights and kills a smaller creature")
        void fightsAndKillsSmallerCreature() {
            // Grizzly Bears is 2/2, Territorial Allosaurus is 5/5
            Permanent target = addCreature(player2);
            castKicked(target.getId());
            harness.passBothPriorities(); // resolve ETB trigger

            // Grizzly Bears should be dead (took 5 damage with 2 toughness)
            harness.assertInGraveyard(player2, "Grizzly Bears");
            // Allosaurus should survive (took 2 damage with 5 toughness)
            harness.assertOnBattlefield(player1, "Territorial Allosaurus");
        }

        @Test
        @DisplayName("Both creatures die when fighting equal-power creature")
        void bothDieWhenFightingEqualPower() {
            // Hill Giant is 3/3, but let's use a 5/5
            // Add another Territorial Allosaurus as the target
            Permanent target = addSpecificCreature(player2, new TerritorialAllosaurus());
            castKicked(target.getId());
            harness.passBothPriorities(); // resolve ETB trigger

            // Both 5/5 creatures should die
            harness.assertInGraveyard(player2, "Territorial Allosaurus");
            harness.assertNotOnBattlefield(player1, "Territorial Allosaurus");
        }

        @Test
        @DisplayName("Can fight own creature")
        void canFightOwnCreature() {
            Permanent ownCreature = addCreature(player1);
            castKicked(ownCreature.getId());
            harness.passBothPriorities(); // resolve ETB trigger

            // Own Grizzly Bears should be dead (took 5 damage with 2 toughness)
            harness.assertInGraveyard(player1, "Grizzly Bears");
            // Allosaurus should survive (took 2 damage with 5 toughness)
            harness.assertOnBattlefield(player1, "Territorial Allosaurus");
        }
    }

    // ===== Helpers =====

    private Permanent addCreature(Player player) {
        return addSpecificCreature(player, new GrizzlyBears());
    }

    private Permanent addSpecificCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void castKicked(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new TerritorialAllosaurus()));
        // Base cost {2}{G}{G} + Kicker {2}{G} = {4}{G}{G}{G}
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castKickedCreature(player1, 0, targetId);
        harness.passBothPriorities(); // resolve creature spell
    }
}
