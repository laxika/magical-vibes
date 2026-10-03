package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.f.FuneralCharm;
import com.github.laxika.magicalvibes.cards.l.LatchkeyFaerie;
import com.github.laxika.magicalvibes.cards.m.MudbuttonClanger;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AuntiesSnitch.class, MudbuttonClanger.class, LatchkeyFaerie.class, ElvishWarrior.class,
        FuneralCharm.class})
class AuntiesSnitchTest extends BaseCardTest {

    private Card putSnitchInGraveyard() {
        Card snitch = new AuntiesSnitch();
        harness.setGraveyard(player1, List.of(snitch));
        return snitch;
    }

    private Permanent addReadyAttacker(Card card) {
        Permanent perm = addCreatureReady(player1, card);
        perm.setAttacking(true);
        return perm;
    }

    private void runCombatDamage() {
        resolveCombat();
        harness.passBothPriorities(); // resolve the graveyard trigger (MayEffect prompt)
    }

    @Test
    @DisplayName("A Goblin dealing combat damage lets you return the Snitch from graveyard to hand")
    void goblinCombatDamageReturnsSnitch() {
        Card snitch = putSnitchInGraveyard();
        addReadyAttacker(new MudbuttonClanger());
        harness.setLife(player2, 20);

        runCombatDamage();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(snitch);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(snitch);
    }

    @Test
    @DisplayName("Declining the may ability leaves the Snitch in the graveyard")
    void decliningLeavesSnitchInGraveyard() {
        Card snitch = putSnitchInGraveyard();
        addReadyAttacker(new MudbuttonClanger());
        harness.setLife(player2, 20);

        runCombatDamage();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(snitch);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(snitch);
    }

    @Test
    @DisplayName("A creature that is neither Goblin nor Rogue does not trigger the Snitch")
    void nonGoblinRogueDoesNotTrigger() {
        Card snitch = putSnitchInGraveyard();
        addReadyAttacker(new ElvishWarrior());
        harness.setLife(player2, 20);

        runCombatDamage();

        // No may ability was queued; the Snitch stays in the graveyard.
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(snitch);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(snitch);
    }

    @Test
    @DisplayName("A Rogue dealing combat damage also lets you return the Snitch from graveyard to hand")
    void rogueCombatDamageReturnsSnitch() {
        Card snitch = putSnitchInGraveyard();
        addReadyAttacker(new LatchkeyFaerie());
        harness.setLife(player2, 20);

        runCombatDamage();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(snitch);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(snitch);
    }

    @Test
    @DisplayName("Combat damage from a Goblin enables casting the Snitch for its prowl cost")
    void prowlCastAfterGoblinCombatDamage() {
        addReadyAttacker(new MudbuttonClanger());
        harness.setLife(player2, 20);

        resolveCombat();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new AuntiesSnitch()));
        harness.addMana(player1, ManaColor.BLACK, 2); // prowl {1}{B}
        harness.castWithProwl(player1, 0, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Auntie's Snitch");
    }

    @Test
    @DisplayName("Prowl is unavailable after combat damage from a creature without the required subtype")
    void prowlUnavailableAfterNonGoblinRogueCombatDamage() {
        addReadyAttacker(new ElvishWarrior());
        harness.setLife(player2, 20);

        resolveCombat();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new AuntiesSnitch()));
        harness.addMana(player1, ManaColor.BLACK, 2); // enough for prowl {1}{B}

        assertThatThrownBy(() -> harness.castWithProwl(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Auntie's Snitch cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        addCreatureReady(player2, new AuntiesSnitch());
        addReadyAttacker(new ElvishWarrior());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Combat damage from a Rogue enables prowl even after that creature leaves play")
    void prowlCastAfterRogueLeavesBattlefield() {
        Permanent rogue = addReadyAttacker(new LatchkeyFaerie());

        resolveCombat();
        gd.playerBattlefields.get(player1.getId()).remove(rogue);
        gd.playerGraveyards.get(player1.getId()).add(rogue.getCard());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new AuntiesSnitch()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castWithProwl(player1, 0, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Auntie's Snitch");
    }

    @Test
    @DisplayName("The Snitch can be cast normally without dealing combat damage")
    void normalCastWithoutCombatDamage() {
        harness.setHand(player1, List.of(new AuntiesSnitch()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Auntie's Snitch");
    }

    @Test
    @DisplayName("Prowl cannot be paid with only one black mana")
    void prowlRequiresTwoMana() {
        addReadyAttacker(new MudbuttonClanger());
        resolveCombat();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new AuntiesSnitch()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castWithProwl(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Auntie's Snitch");
        harness.assertNotOnBattlefield(player1, "Auntie's Snitch");
    }

    @Test
    @DisplayName("An opponent's Goblin dealing combat damage does not return your Snitch")
    void opponentsGoblinDoesNotTrigger() {
        Card snitch = putSnitchInGraveyard();
        Permanent attacker = addCreatureReady(player2, new MudbuttonClanger());
        attacker.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(snitch);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(snitch);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An old graveyard trigger cannot return a Snitch that was returned and discarded again")
    void oldTriggerLosesTrackAfterReturnAndDiscard() {
        Card snitch = putSnitchInGraveyard();
        addReadyAttacker(new MudbuttonClanger());
        addReadyAttacker(new MudbuttonClanger());
        harness.setHand(player1, List.of(new FuneralCharm()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        runCombatDamage();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).contains(snitch);
        assertThat(gd.stack).hasSize(1);

        harness.castInstant(player1, 0, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        resolveAllTriggers();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(snitch);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(snitch);
    }
}
