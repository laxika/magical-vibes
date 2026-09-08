package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.o.OrcishHealer;
import com.github.laxika.magicalvibes.cards.w.WallOfPineNeedles;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Gravebind.class, BalduvianBears.class, OrcishHealer.class, WallOfPineNeedles.class, ZuranOrb.class})
class GravebindTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving marks the target so it can't be regenerated and schedules a draw")
    void marksTargetAndSchedulesDraw() {
        Permanent healer = addRegeneratingCreature(player2);
        harness.setHand(player1, List.of(new Gravebind()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, healer.getId());

        assertThat(healer.isCantRegenerateThisTurn()).isTrue();
        assertThat(healer.getRegenerationShield()).isEqualTo(1);

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Fizzles without scheduling a draw when the target leaves before resolution")
    void fizzlesWithoutSchedulingDrawWhenTargetLeavesBeforeResolution() {
        Permanent healer = addRegeneratingCreature(player2);
        harness.setHand(player1, List.of(new Gravebind()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, healer.getId());
        gd.playerBattlefields.get(player2.getId()).remove(healer);
        harness.passBothPriorities();

        assertThat(healer.isCantRegenerateThisTurn()).isFalse();
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("A marked creature dies in combat despite its regeneration shield")
    void markedCreatureDiesInCombatDespiteShield() {
        Permanent healer = addRegeneratingCreature(player2);
        harness.setHand(player1, List.of(new Gravebind()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, healer.getId());

        addCreatureReady(player1, new BalduvianBears());
        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Orcish Healer");
        harness.assertInGraveyard(player2, "Orcish Healer");
    }

    @Test
    @DisplayName("The scheduled draw resolves at the next upkeep")
    void drawResolvesAtNextUpkeep() {
        Permanent healer = addRegeneratingCreature(player2);
        harness.setHand(player1, List.of(new Gravebind()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, healer.getId());

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        gd.activePlayerId = player2.getId();
        harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("The can't-be-regenerated mark clears during end-of-turn cleanup")
    void markClearsAtEndOfTurn() {
        Permanent healer = addRegeneratingCreature(player2);
        harness.setHand(player1, List.of(new Gravebind()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, healer.getId());
        assertThat(healer.isCantRegenerateThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(healer.isCantRegenerateThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new BalduvianBears());
        Permanent orb = harness.addToBattlefieldAndReturn(player1, new ZuranOrb());
        harness.setHand(player1, List.of(new Gravebind()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, orb.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("A regeneration shield created afterward is not applied to the marked creature")
    void laterRegenerationShieldCannotSaveMarkedCreature() {
        Permanent wall = addCreatureReady(player2, new WallOfPineNeedles());
        harness.setHand(player1, List.of(new Gravebind()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, wall.getId());

        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        assertThat(wall.getRegenerationShield()).isEqualTo(1);

        wall.setMarkedDamage(3);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player2, "Wall of Pine Needles");
        harness.assertInGraveyard(player2, "Wall of Pine Needles");
    }

    private Permanent addRegeneratingCreature(Player player) {
        Permanent perm = addCreatureReady(player, new OrcishHealer());
        perm.setRegenerationShield(1);
        return perm;
    }
}
