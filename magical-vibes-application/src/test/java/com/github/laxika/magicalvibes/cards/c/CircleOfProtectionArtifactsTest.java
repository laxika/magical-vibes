package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DragonEngine;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CircleOfProtectionArtifacts.class, DragonEngine.class, GrizzlyBears.class, RodOfRuin.class})
class CircleOfProtectionArtifactsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability prompts for an artifact source choice")
    void resolvingAbilityPromptsForArtifactSource() {
        addReadyCircle(player1);
        Permanent golem = addReadyArtifactCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(golem.getId());
    }

    @Test
    @DisplayName("Choosing an artifact source records a one-shot prevention shield")
    void choosingArtifactSourceRecordsShield() {
        addReadyCircle(player1);
        Permanent golem = addReadyArtifactCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, golem.getId());

        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.playerId().equals(player1.getId()) && s.sourceId().equals(golem.getId()));
    }

    @Test
    @DisplayName("Prevents the next combat damage from the chosen artifact source and consumes the shield")
    void preventsNextCombatDamageAndConsumesShield() {
        harness.setLife(player1, 20);
        addReadyCircle(player1);
        Permanent golem = addReadyArtifactCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, golem.getId());

        golem.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Non-artifact permanents are not valid source choices")
    void nonArtifactSourceNotValid() {
        addReadyCircle(player1);
        addReadyNonArtifactCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
        assertThat(gameLogContains("No permanents on the battlefield")).isTrue();
    }

    @Test
    @DisplayName("Prevents the next noncombat damage from the chosen artifact source")
    void preventsNextNoncombatDamage() {
        harness.setLife(player1, 20);
        addReadyCircle(player1);
        Permanent rod = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, rod.getId());

        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("A different artifact source still deals damage")
    void differentArtifactSourceStillDealsDamage() {
        harness.setLife(player1, 20);
        addReadyCircle(player1);
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.activateAbility(player2, 1, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.sourceId().equals(chosen.getId()));
    }

    @Test
    @DisplayName("Choosing an artifact spell protects the permanent it becomes")
    void chosenArtifactSpellProtectsPermanentItBecomes() {
        harness.setLife(player1, 20);
        addReadyCircle(player1);
        RodOfRuin rod = new RodOfRuin();
        harness.setHand(player2, List.of(rod));
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.forceActivePlayer(player2);
        harness.castArtifact(player2, 0);
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(rod.getId());

        harness.handlePermanentChosen(player1, rod.getId());
        harness.passBothPriorities();

        Permanent resolvedRod = findPermanent(player2, "Rod of Ruin");
        int rodIndex = gd.playerBattlefields.get(player2.getId()).indexOf(resolvedRod);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.activateAbility(player2, rodIndex, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        addReadyCircle(player1);
        Permanent golem = addReadyArtifactCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, golem.getId());

        assertThat(gd.playerSourceNextDamageShields).isNotEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    private Permanent addReadyCircle(Player player) {
        return addCreatureReady(player, new CircleOfProtectionArtifacts());
    }

    private Permanent addReadyArtifactCreature(Player player) {
        return addCreatureReady(player, new DragonEngine());
    }

    private Permanent addReadyNonArtifactCreature(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }
}
