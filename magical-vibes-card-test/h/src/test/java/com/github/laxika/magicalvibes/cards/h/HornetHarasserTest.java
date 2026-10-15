package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CloudcrownOak;
import com.github.laxika.magicalvibes.cards.l.LowlandOaf;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HornetHarasser.class, WoodlandChangeling.class, CloudcrownOak.class, LowlandOaf.class})
class HornetHarasserTest extends BaseCardTest {

    /**
     * Sets up combat where Hornet Harasser (player1, 2/2) attacks and is blocked by a 3/3 creature (player2),
     * so the Harasser dies from combat damage.
     */
    private void setupCombatWhereHarasserDies() {
        Permanent harasserPerm = findPermanent(player1, "Hornet Harasser");
        harasserPerm.setSummoningSick(false);
        harasserPerm.setAttacking(true);

        Permanent blockerPerm = addCreatureReady(player2, new LowlandOaf());
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
    }

    @Test
    @DisplayName("Death trigger prompts controller to choose a target creature")
    void deathTriggerPromptsForTarget() {
        harness.addToBattlefield(player1, new HornetHarasser());
        harness.addToBattlefield(player2, new WoodlandChangeling());
        setupCombatWhereHarasserDies();

        resolveCombat();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Hornet Harasser");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Death trigger gives target creature -2/-2 until end of turn")
    void deathTriggerDebuffsTarget() {
        harness.addToBattlefield(player1, new HornetHarasser());

        harness.addToBattlefield(player2, new CloudcrownOak());
        UUID bearId = harness.getPermanentId(player2, "Cloudcrown Oak");

        setupCombatWhereHarasserDies();
        resolveCombat();

        harness.handlePermanentChosen(player1, bearId);
        harness.passBothPriorities(); // Resolve trigger

        Permanent bear = findPermanent(player2, "Cloudcrown Oak");
        assertThat(bear.getPowerModifier()).isEqualTo(-2);
        assertThat(bear.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("-2/-2 kills a 2/2 creature")
    void debuffKillsTwoTwoCreature() {
        harness.addToBattlefield(player1, new HornetHarasser());
        harness.addToBattlefield(player2, new WoodlandChangeling());
        UUID bearId = harness.getPermanentId(player2, "Woodland Changeling");

        setupCombatWhereHarasserDies();
        resolveCombat();

        harness.handlePermanentChosen(player1, bearId);
        harness.passBothPriorities(); // Resolve trigger

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(bearId));
        harness.assertInGraveyard(player2, "Woodland Changeling");
    }

    @Test
    @DisplayName("Debuff wears off at cleanup")
    void debuffWearsOff() {
        harness.addToBattlefield(player1, new HornetHarasser());

        harness.addToBattlefield(player2, new CloudcrownOak());
        UUID bearId = harness.getPermanentId(player2, "Cloudcrown Oak");

        setupCombatWhereHarasserDies();
        resolveCombat();

        harness.handlePermanentChosen(player1, bearId);
        harness.passBothPriorities(); // Resolve trigger

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = findPermanent(player2, "Cloudcrown Oak");
        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Death trigger fizzles when the target leaves before resolution")
    void abilityFizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player1, new HornetHarasser());
        harness.addToBattlefield(player2, new WoodlandChangeling());
        UUID bearId = harness.getPermanentId(player2, "Woodland Changeling");

        setupCombatWhereHarasserDies();
        resolveCombat();

        harness.handlePermanentChosen(player1, bearId);

        GameData gd = harness.getGameData();
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(bearId));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Death trigger can target a creature its controller controls")
    void deathTriggerCanTargetOwnCreature() {
        harness.addToBattlefield(player1, new HornetHarasser());
        harness.addToBattlefield(player1, new WoodlandChangeling());
        UUID targetId = harness.getPermanentId(player1, "Woodland Changeling");
        setupCombatWhereHarasserDies();

        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(targetId);
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Woodland Changeling");
        assertThat(countPermanents(player1, "Woodland Changeling")).isZero();
    }

    @Test
    @DisplayName("Death trigger has no target when all creatures die simultaneously")
    void noTargetWhenBothCombatantsDie() {
        Permanent harasser = addCreatureReady(player1, new HornetHarasser());
        Permanent blocker = addCreatureReady(player2, new WoodlandChangeling());
        harasser.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player1, "Hornet Harasser");
        harness.assertInGraveyard(player2, "Woodland Changeling");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exiling Hornet Harasser does not trigger its death ability")
    void exileDoesNotTriggerDeathAbility() {
        Permanent harasser = harness.addToBattlefieldAndReturn(player1, new HornetHarasser());
        harness.addToBattlefield(player2, new WoodlandChangeling());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToExile(gd, harasser));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Hornet Harasser")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player2, "Woodland Changeling").getToughnessModifier()).isZero();
    }
}
