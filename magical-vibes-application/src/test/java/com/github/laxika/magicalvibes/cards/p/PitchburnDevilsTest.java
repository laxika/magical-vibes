package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LilianaOfTheVeil;
import com.github.laxika.magicalvibes.cards.s.SilentDeparture;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PitchburnDevils.class, GrizzlyBears.class, WrathOfGod.class,
        LilianaOfTheVeil.class, SilentDeparture.class})
class PitchburnDevilsTest extends BaseCardTest {

    /**
     * Sets up combat where Pitchburn Devils (player1) attacks and is blocked by a bigger creature (player2).
     * Devils (3/3) will die from combat damage against a 4/4.
     */
    private void setupCombatWhereDevilsDie() {
        Permanent devilsPerm = findPermanent(player1, "Pitchburn Devils");
        devilsPerm.setSummoningSick(false);
        devilsPerm.setAttacking(true);

        GrizzlyBears bigCreature = new GrizzlyBears();
        bigCreature.setPower(4);
        bigCreature.setToughness(4);
        Permanent blockerPerm = addCreatureReady(player2, bigCreature);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Pitchburn Devils puts it on the battlefield")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new PitchburnDevils()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Pitchburn Devils");
    }

    // ===== Death trigger — target creature =====

    @Test
    @DisplayName("When Pitchburn Devils dies in combat, controller is prompted to choose any target")
    void deathTriggerPromptsForTarget() {
        harness.addToBattlefield(player1, new PitchburnDevils());
        harness.addToBattlefield(player2, new GrizzlyBears());
        setupCombatWhereDevilsDie();

        resolveCombat();

        GameData gd = harness.getGameData();

        harness.assertInGraveyard(player1, "Pitchburn Devils");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Death trigger deals 3 damage to chosen creature and destroys it if lethal")
    void deathTriggerDeals3DamageAndKillsCreature() {
        harness.addToBattlefield(player1, new PitchburnDevils());

        GrizzlyBears bears = new GrizzlyBears();
        bears.setPower(3);
        bears.setToughness(3);
        harness.addToBattlefield(player2, bears);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereDevilsDie();
        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, bearsId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Pitchburn Devils");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bearsId);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bearsId));
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    // ===== Death trigger — target player =====

    @Test
    @DisplayName("Death trigger deals 3 damage to chosen player")
    void deathTriggerDeals3DamageToPlayer() {
        harness.addToBattlefield(player1, new PitchburnDevils());
        harness.setLife(player2, 20);

        setupCombatWhereDevilsDie();
        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, player2.getId());

        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Death trigger can target own controller")
    void deathTriggerCanTargetSelf() {
        harness.addToBattlefield(player1, new PitchburnDevils());
        harness.setLife(player1, 20);

        setupCombatWhereDevilsDie();
        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, player1.getId());

        harness.passBothPriorities();

        harness.assertLife(player1, 17);
    }

    // ===== Death trigger — board wipe =====

    @Test
    @DisplayName("Death trigger after Wrath of God still targets players")
    void deathTriggerAfterWrathTargetsPlayer() {
        harness.addToBattlefield(player1, new PitchburnDevils());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        harness.assertInGraveyard(player1, "Pitchburn Devils");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles when target creature is removed before resolution")
    void abilityFizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player1, new PitchburnDevils());

        GrizzlyBears bears = new GrizzlyBears();
        bears.setPower(3);
        bears.setToughness(3);
        harness.addToBattlefield(player2, bears);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereDevilsDie();
        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, bearsId);

        gd.playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getId().equals(bearsId));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Death trigger can deal lethal damage to a planeswalker")
    void deathTriggerCanTargetPlaneswalker() {
        harness.addToBattlefield(player1, new PitchburnDevils());
        Permanent liliana = harness.enterBattlefieldAndReturn(player2, new LilianaOfTheVeil());
        setupCombatWhereDevilsDie();
        resolveCombat();

        harness.handlePermanentChosen(player1, liliana.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Liliana of the Veil");
        harness.assertNotOnBattlefield(player2, "Liliana of the Veil");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Death trigger can target a creature its controller controls")
    void deathTriggerCanTargetOwnCreature() {
        harness.addToBattlefield(player1, new PitchburnDevils());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        setupCombatWhereDevilsDie();
        resolveCombat();

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returning Pitchburn Devils to hand does not trigger its death ability")
    void returningToHandDoesNotTriggerDeathAbility() {
        Permanent devils = harness.addToBattlefieldAndReturn(player1, new PitchburnDevils());
        harness.setHand(player1, List.of(new SilentDeparture()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, devils.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Pitchburn Devils");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The dying Devils' controller chooses the target, even on an opponent's turn")
    void opponentControlledDevilsChooseTheirTarget() {
        harness.addToBattlefield(player2, new PitchburnDevils());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Pitchburn Devils");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }
}
