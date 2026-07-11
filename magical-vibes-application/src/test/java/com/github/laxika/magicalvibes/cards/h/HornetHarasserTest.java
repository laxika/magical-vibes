package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HornetHarasserTest extends BaseCardTest {

    /**
     * Sets up combat where Hornet Harasser (player1, 2/2) attacks and is blocked by a 3/3 creature (player2),
     * so the Harasser dies from combat damage.
     */
    private void setupCombatWhereHarasserDies() {
        Permanent harasserPerm = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hornet Harasser"))
                .findFirst().orElseThrow();
        harasserPerm.setSummoningSick(false);
        harasserPerm.setAttacking(true);

        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(3);
        bigBear.setToughness(3);
        Permanent blockerPerm = new Permanent(bigBear);
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Death trigger prompts controller to choose a target creature")
    void deathTriggerPromptsForTarget() {
        harness.addToBattlefield(player1, new HornetHarasser());
        harness.addToBattlefield(player2, new GrizzlyBears());
        setupCombatWhereHarasserDies();

        harness.passBothPriorities(); // Combat damage — Harasser dies

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Hornet Harasser"));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Death trigger gives target creature -2/-2 until end of turn")
    void deathTriggerDebuffsTarget() {
        harness.addToBattlefield(player1, new HornetHarasser());

        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(4);
        bigBear.setToughness(4);
        harness.addToBattlefield(player2, bigBear);
        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereHarasserDies();
        harness.passBothPriorities(); // Harasser dies

        harness.handlePermanentChosen(player1, bearId);
        harness.passBothPriorities(); // Resolve trigger

        Permanent bear = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(bearId)).findFirst().orElseThrow();
        assertThat(bear.getPowerModifier()).isEqualTo(-2);
        assertThat(bear.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("-2/-2 kills a 2/2 creature")
    void debuffKillsTwoTwoCreature() {
        harness.addToBattlefield(player1, new HornetHarasser());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereHarasserDies();
        harness.passBothPriorities(); // Harasser dies

        harness.handlePermanentChosen(player1, bearId);
        harness.passBothPriorities(); // Resolve trigger

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(bearId));
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Debuff wears off at cleanup")
    void debuffWearsOff() {
        harness.addToBattlefield(player1, new HornetHarasser());

        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(4);
        bigBear.setToughness(4);
        harness.addToBattlefield(player2, bigBear);
        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereHarasserDies();
        harness.passBothPriorities(); // Harasser dies

        harness.handlePermanentChosen(player1, bearId);
        harness.passBothPriorities(); // Resolve trigger

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(bearId)).findFirst().orElseThrow();
        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Death trigger fizzles when the target leaves before resolution")
    void abilityFizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player1, new HornetHarasser());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereHarasserDies();
        harness.passBothPriorities(); // Harasser dies

        harness.handlePermanentChosen(player1, bearId);

        GameData gd = harness.getGameData();
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(bearId));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }
}
