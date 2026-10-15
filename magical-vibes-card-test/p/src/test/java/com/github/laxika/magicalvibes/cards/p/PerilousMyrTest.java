package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.k.KothOfTheHammer;
import com.github.laxika.magicalvibes.cards.r.RevokeExistence;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PerilousMyr.class, GrizzlyBears.class, WrathOfGod.class, KothOfTheHammer.class, RevokeExistence.class})
class PerilousMyrTest extends BaseCardTest {

    /**
     * Sets up combat where Perilous Myr (player1) attacks and is blocked by a 3/3 creature (player2).
     * Myr (1/1) will die from combat damage.
     */
    private void setupCombatWhereMyrDies() {
        Permanent myrPerm = findPermanent(player1, "Perilous Myr");
        myrPerm.setSummoningSick(false);
        myrPerm.setAttacking(true);

        Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blockerPerm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Casting Perilous Myr puts it on the battlefield")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new PerilousMyr()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Perilous Myr");
    }

    @Test
    @DisplayName("When Perilous Myr dies in combat, controller is prompted to choose any target")
    void deathTriggerPromptsForTarget() {
        harness.addToBattlefield(player1, new PerilousMyr());
        harness.addToBattlefield(player2, new GrizzlyBears());
        setupCombatWhereMyrDies();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        harness.assertInGraveyard(player1, "Perilous Myr");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Death trigger deals 2 damage to chosen creature and destroys it if lethal")
    void deathTriggerDeals2DamageAndKillsCreature() {
        harness.addToBattlefield(player1, new PerilousMyr());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereMyrDies();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, bearsId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Perilous Myr");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bearsId);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bearsId));
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Death trigger deals 2 damage to chosen player")
    void deathTriggerDeals2DamageToPlayer() {
        harness.addToBattlefield(player1, new PerilousMyr());
        harness.setLife(player2, 20);

        setupCombatWhereMyrDies();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, player2.getId());

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Death trigger can target own controller")
    void deathTriggerCanTargetSelf() {
        harness.addToBattlefield(player1, new PerilousMyr());
        harness.setLife(player1, 20);

        setupCombatWhereMyrDies();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, player1.getId());

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Death trigger still targets a player after Wrath of God destroys all creatures")
    void deathTriggerAfterWrathTargetsPlayer() {
        harness.addToBattlefield(player1, new PerilousMyr());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        harness.assertInGraveyard(player1, "Perilous Myr");

        // Any-target trigger should still fire (can target players)
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Ability fizzles when target creature is removed before resolution")
    void abilityFizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player1, new PerilousMyr());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereMyrDies();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, bearsId);

        gd.playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getId().equals(bearsId));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Death trigger deals 2 damage directly to a planeswalker")
    void deathTriggerDealsDamageToPlaneswalker() {
        harness.addToBattlefield(player1, new PerilousMyr());
        Permanent koth = harness.addToBattlefieldAndReturn(player2, new KothOfTheHammer());
        koth.setCounterCount(CounterType.LOYALTY, 3);
        setupCombatWhereMyrDies();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, koth.getId());
        harness.passBothPriorities();

        assertThat(koth.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Koth of the Hammer");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Death trigger can target a creature controlled by its controller")
    void deathTriggerCanTargetOwnCreature() {
        harness.addToBattlefield(player1, new PerilousMyr());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        setupCombatWhereMyrDies();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Exiling Perilous Myr does not trigger its death ability")
    void exileDoesNotTriggerDeathAbility() {
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new PerilousMyr());
        harness.setHand(player1, List.of(new RevokeExistence()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, myr.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Perilous Myr");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(myr.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(myr.getCard());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
