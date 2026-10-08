package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.Gigapede;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.m.MistformWall;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CommandoRaid.class, GlorySeeker.class, MistformWall.class, Gigapede.class})
class CommandoRaidTest extends BaseCardTest {

    @Test
    @DisplayName("Grants a combat-damage trigger that may deal damage equal to the creature's power")
    void combatDamageTriggerDealsPowerDamageToDamagedPlayersCreature() {
        Permanent attacker = addCreatureReady(player1, new GlorySeeker());
        Permanent ownCreature = addCreatureReady(player1, new GlorySeeker());
        Permanent damagedPlayersCreature = addCreatureReady(player2, new MistformWall());
        castOn(attacker);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(damagedPlayersCreature.getId());
        harness.handlePermanentChosen(player1, damagedPlayersCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(damagedPlayersCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(ownCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Uses the creature's power when the granted trigger resolves")
    void usesPowerAtTriggerResolution() {
        Permanent attacker = addCreatureReady(player1, new GlorySeeker());
        Permanent damagedPlayersCreature = addCreatureReady(player2, new MistformWall());
        castOn(attacker);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, damagedPlayersCreature.getId());
        attacker.setPowerModifier(1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(damagedPlayersCreature.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining the granted combat-damage trigger deals no additional damage")
    void decliningCombatDamageTriggerDealsNoAdditionalDamage() {
        Permanent attacker = addCreatureReady(player1, new GlorySeeker());
        Permanent damagedPlayersCreature = addCreatureReady(player2, new MistformWall());
        castOn(attacker);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, damagedPlayersCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(damagedPlayersCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The granted combat-damage trigger expires at end of turn")
    void grantedTriggerExpiresAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new GlorySeeker());
        Permanent damagedPlayersCreature = addCreatureReady(player2, new MistformWall());
        castOn(attacker);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(damagedPlayersCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target a creature controlled by an opponent")
    void cannotTargetOpponentCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new GlorySeeker());
        harness.setHand(player1, List.of(new CommandoRaid()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The granted ability cannot target a creature with shroud")
    void shroudCreatureIsNotALegalTarget() {
        Permanent attacker = addCreatureReady(player1, new GlorySeeker());
        Permanent shroudCreature = addCreatureReady(player2, new Gigapede());
        Permanent legalTarget = addCreatureReady(player2, new MistformWall());
        castOn(attacker);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(legalTarget.getId()).doesNotContain(shroudCreature.getId());
    }

    @Test
    @DisplayName("Combat damage to a creature does not trigger the granted ability")
    void blockedCreatureDoesNotTrigger() {
        Permanent attacker = addCreatureReady(player1, new GlorySeeker());
        Permanent blocker = addCreatureReady(player2, new MistformWall());
        castOn(attacker);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The granted ability cannot be put on the stack without a legal target")
    void noCreaturesMeansNoTargetedAbility() {
        Permanent attacker = addCreatureReady(player1, new GlorySeeker());
        castOn(attacker);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castOn(Permanent target) {
        Set<TurnStep> stops = Set.of(TurnStep.PRECOMBAT_MAIN, TurnStep.POSTCOMBAT_MAIN,
                TurnStep.DECLARE_BLOCKERS, TurnStep.COMBAT_DAMAGE);
        gd.playerAutoStopSteps.put(player1.getId(), stops);
        gd.playerAutoStopSteps.put(player2.getId(), stops);
        harness.setHand(player1, List.of(new CommandoRaid()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
