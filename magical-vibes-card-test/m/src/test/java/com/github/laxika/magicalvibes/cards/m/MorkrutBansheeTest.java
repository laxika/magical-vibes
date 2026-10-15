package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.b.BrimstoneVolley;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.f.FortressCrab;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MorkrutBanshee.class, WalkingCorpse.class, BrimstoneVolley.class, FortressCrab.class})
class MorkrutBansheeTest extends BaseCardTest {

    @Test
    @DisplayName("No ETB trigger and no target prompt without morbid")
    void noEffectWithoutMorbid() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new MorkrutBanshee()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player2, "Walking Corpse");
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        // No morbid — no ETB trigger fires and no target is ever chosen (CR 603.4)
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();

        // Walking Corpse should be unaffected
        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(targetId))
                .findFirst().orElseThrow();
        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Morbid met — target chosen at trigger time gets -4/-4")
    void morbidGivesMinusFourMinusFour() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new MorkrutBanshee()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        // Simulate morbid
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        UUID targetId = harness.getPermanentId(player2, "Walking Corpse");
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt
        harness.handlePermanentChosen(player1, targetId); // ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB

        // Walking Corpse (2/2) gets -4/-4 → lethal, is put into the graveyard as a state-based action
        harness.assertNotOnBattlefield(player2, "Walking Corpse");
    }

    @Test
    @DisplayName("Morbid met — can target own creature at trigger time")
    void morbidReducesBigCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.setHand(player1, List.of(new MorkrutBanshee()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        UUID targetId = harness.getPermanentId(player1, "Walking Corpse");
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt
        harness.handlePermanentChosen(player1, targetId); // ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB

        // Walking Corpse (2/2) with -4/-4 is put into the graveyard for having zero or less toughness
        harness.assertNotOnBattlefield(player1, "Walking Corpse");
    }

    @Test
    @DisplayName("Killing a creature with Brimstone Volley enables morbid ETB")
    void actualCreatureDeathEnablesMorbid() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        WalkingCorpse bears1 = new WalkingCorpse();
        WalkingCorpse bears2 = new WalkingCorpse();
        harness.addToBattlefield(player2, bears1);
        harness.addToBattlefield(player2, bears2);
        harness.setHand(player1, List.of(new BrimstoneVolley(), new MorkrutBanshee()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        // Kill first Walking Corpse with BrimstoneVolley
        UUID bears1Id = harness.getPermanentId(player2, "Walking Corpse");
        harness.castAndResolveInstant(player1, 0, bears1Id); // resolve BrimstoneVolley — morbid now active

        // Cast Morkrut Banshee, then choose the second Bears as the trigger target
        UUID bears2Id = harness.getPermanentId(player2, "Walking Corpse");
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt
        harness.handlePermanentChosen(player1, bears2Id); // ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB — -4/-4 kills Bears

        harness.assertNotOnBattlefield(player2, "Walking Corpse");
    }

    @Test
    @DisplayName("Trigger target prompt only offers creatures")
    void triggerPromptOffersOnlyCreatures() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new MorkrutBanshee()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        UUID bearsId = harness.getPermanentId(player2, "Walking Corpse");
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        UUID bansheeId = harness.getPermanentId(player1, "Morkrut Banshee");
        assertThat(choice.validIds()).containsExactlyInAnyOrder(bearsId, bansheeId);
    }

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new MorkrutBanshee()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        UUID targetId = harness.getPermanentId(player2, "Walking Corpse");
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt
        harness.handlePermanentChosen(player1, targetId); // ETB trigger on stack

        // Remove target before ETB resolves
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities(); // resolve ETB — fizzles

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Morbid forces Banshee to target itself when it is the only creature")
    void targetsItselfWhenItIsTheOnlyCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);
        harness.setHand(player1, List.of(new MorkrutBanshee()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        UUID bansheeId = harness.getPermanentId(player1, "Morkrut Banshee");
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(bansheeId);
        harness.handlePermanentChosen(player1, bansheeId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Morkrut Banshee");
        harness.assertInGraveyard(player1, "Morkrut Banshee");
    }

    @Test
    @DisplayName("Both power and toughness are reduced only until end of turn")
    void reductionExpiresAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new FortressCrab());
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);
        harness.setHand(player1, List.of(new MorkrutBanshee()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        UUID crabId = harness.getPermanentId(player2, "Fortress Crab");

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, crabId);
        harness.passBothPriorities();

        Permanent crab = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(crabId)).findFirst().orElseThrow();
        assertThat(harness.getGameQueryService().getEffectivePower(gd, crab)).isEqualTo(-3);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, crab)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.assertOnBattlefield(player2, "Fortress Crab");
        assertThat(harness.getGameQueryService().getEffectivePower(gd, crab)).isEqualTo(1);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, crab)).isEqualTo(6);
    }

    @Test
    @DisplayName("Can cast without target and enters battlefield normally")
    void canCastWithoutTarget() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MorkrutBanshee()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Morkrut Banshee");
        assertThat(gd.stack).isEmpty();
    }
}
