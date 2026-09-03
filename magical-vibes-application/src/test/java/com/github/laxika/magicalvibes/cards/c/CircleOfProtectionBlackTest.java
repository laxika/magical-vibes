package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KrovikanHorror;
import com.github.laxika.magicalvibes.cards.p.Pestilence;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CircleOfProtectionBlack.class, GrizzlyBears.class, KrovikanHorror.class,
        Pestilence.class, ScatheZombies.class})
class CircleOfProtectionBlackTest extends BaseCardTest {
    private static final String PESTILENCE_MANA_COST = "{2}{B}{B}";

    @Test
    @DisplayName("Resolving the ability prompts for a black source choice")
    void resolvingAbilityPromptsForBlackSource() {
        addReadyCircle(player1);
        addReadyBlackCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Choosing a black source records a one-shot prevention shield")
    void choosingBlackSourceRecordsShield() {
        addReadyCircle(player1);
        Permanent zombie = addReadyBlackCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, zombie.getId());

        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.playerId().equals(player1.getId()) && s.sourceId().equals(zombie.getId()));
    }

    @Test
    @DisplayName("Prevents the next combat damage from the chosen source and consumes the shield")
    void preventsNextCombatDamageAndConsumesShield() {
        harness.setLife(player1, 20);
        addReadyCircle(player1);
        Permanent zombie = addReadyBlackCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, zombie.getId());

        zombie.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Prevents the next noncombat damage from the chosen black source")
    void preventsNextNoncombatDamageAndConsumesShield() {
        harness.setLife(player1, 20);
        addReadyCircle(player1);
        Permanent horror = addReadyBlackDamageSource(player2);
        Permanent fodder = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, horror.getId());

        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.handlePermanentChosen(player2, fodder.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("A permanent spell remains the chosen source after it resolves")
    void preventsDamageFromPermanentSpellAfterItResolves() {
        harness.setLife(player1, 20);
        addReadyCircle(player1);
        Pestilence pestilenceSpell = new Pestilence();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, pestilenceSpell, PESTILENCE_MANA_COST);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, pestilenceSpell.getId());
        harness.passBothPriorities();

        Permanent pestilence = findPermanent(player2, Pestilence.class.getSimpleName());
        int pestilenceIndex = gd.playerBattlefields.get(player2.getId()).indexOf(pestilence);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.activateAbility(player2, pestilenceIndex, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Only the chosen source is prevented; a different black source still deals damage")
    void differentSourceStillDealsDamage() {
        harness.setLife(player1, 20);
        addReadyCircle(player1);
        Permanent chosen = addReadyBlackCreature(player2);
        Permanent other = addReadyBlackCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        other.setAttacking(true);
        resolveCombat(player2);

        // The unchosen 2/2 deals its damage; the shield is untouched
        harness.assertLife(player1, 18);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.sourceId().equals(chosen.getId()));
    }

    @Test
    @DisplayName("Non-black permanents are not valid source choices")
    void nonBlackSourceNotValid() {
        addReadyCircle(player1);
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("No permanents on the battlefield"));
    }

    @Test
    @DisplayName("Shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        addReadyCircle(player1);
        Permanent zombie = addReadyBlackCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, zombie.getId());

        assertThat(gd.playerSourceNextDamageShields).isNotEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    private Permanent addReadyCircle(Player player) {
        return harness.addToBattlefieldAndReturn(player, new CircleOfProtectionBlack());
    }

    private Permanent addReadyBlackCreature(Player player) {
        return addCreatureReady(player, new ScatheZombies());
    }

    private Permanent addReadyBlackDamageSource(Player player) {
        return addCreatureReady(player, new KrovikanHorror());
    }
}
