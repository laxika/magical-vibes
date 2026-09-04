package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KrovikanHorror;
import com.github.laxika.magicalvibes.cards.p.Pestilence;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.cards.a.AdarkarUnicorn;
import com.github.laxika.magicalvibes.cards.m.MoorFiend;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredSwamp;
import com.github.laxika.magicalvibes.cards.t.TouchOfDeath;
import com.github.laxika.magicalvibes.cards.w.WitheringWisps;
import com.github.laxika.magicalvibes.model.GameLogEntry;
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

@CardUsed({CircleOfProtectionBlack.class, AdarkarUnicorn.class, MoorFiend.class, TouchOfDeath.class, WitheringWisps.class, SnowCoveredSwamp.class, GrizzlyBears.class, KrovikanHorror.class, Pestilence.class, ScatheZombies.class})
class CircleOfProtectionBlackTest extends BaseCardTest {

    private static final String PESTILENCE_MANA_COST = "{2}{B}{B}";

    @Test
    @DisplayName("Resolving the ability prompts for a black source choice")
    void resolvingAbilityPromptsForBlackSource() {
        addReadyCircle(player1);
        Permanent blackSource = addCreatureReady(player2, new MoorFiend());
        Permanent nonBlackSource = addCreatureReady(player2, new AdarkarUnicorn());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(blackSource.getId()).doesNotContain(nonBlackSource.getId());
    }

    @Test
    @DisplayName("Choosing a black source records a one-shot prevention shield")
    void choosingBlackSourceRecordsShield() {
        addReadyCircle(player1);
        Permanent blackSource = addCreatureReady(player2, new MoorFiend());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, blackSource.getId());

        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.playerId().equals(player1.getId()) && s.sourceId().equals(blackSource.getId()));
    }

    @Test
    @DisplayName("Prevents the next combat damage from the chosen source and consumes the shield")
    void preventsNextCombatDamageAndConsumesShield() {
        harness.setLife(player1, 20);
        addReadyCircle(player1);
        Permanent blackSource = addCreatureReady(player2, new MoorFiend());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, blackSource.getId());

        blackSource.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Prevents the next noncombat damage from a chosen black spell")
    void preventsNextNoncombatDamageFromChosenBlackSpell() {
        harness.setLife(player1, 20);
        addReadyCircle(player1);
        TouchOfDeath blackSpell = new TouchOfDeath();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(blackSpell));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(blackSpell.getId());
        harness.handlePermanentChosen(player1, blackSpell.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Only the chosen source is prevented; a different black source still deals damage")
    void differentSourceStillDealsDamage() {
        harness.setLife(player1, 20);
        addReadyCircle(player1);
        Permanent chosen = addCreatureReady(player2, new MoorFiend());
        Permanent other = addCreatureReady(player2, new MoorFiend());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        other.setAttacking(true);
        resolveCombat(player2);

        // The unchosen 3/3 deals its damage; the shield is untouched.
        harness.assertLife(player1, 17);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.sourceId().equals(chosen.getId()));
    }

    @Test
    @DisplayName("Prevents only the next damage event from the chosen source")
    void preventsOnlyNextDamageEventFromChosenSource() {
        harness.setLife(player1, 20);
        addReadyCircle(player1);
        Permanent wisps = addCreatureReady(player2, new WitheringWisps());
        addCreatureReady(player2, new SnowCoveredSwamp());
        addCreatureReady(player2, new SnowCoveredSwamp());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, wisps.getId());

        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Non-black permanents are not valid source choices")
    void nonBlackSourceNotValid() {
        addReadyCircle(player1);
        addCreatureReady(player2, new AdarkarUnicorn());
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
        Permanent blackSource = addCreatureReady(player2, new MoorFiend());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, blackSource.getId());

        assertThat(gd.playerSourceNextDamageShields).isNotEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    private Permanent addReadyCircle(Player player) {
        return addCreatureReady(player, new CircleOfProtectionBlack());
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

    private Permanent addReadyBlackCreature(Player player) {
        return addCreatureReady(player, new ScatheZombies());
    }

    private Permanent addReadyBlackDamageSource(Player player) {
        return addCreatureReady(player, new KrovikanHorror());
    }
}
