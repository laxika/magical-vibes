package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.Brushland;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RuneOfProtectionLandsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability prompts for a land source choice")
    void resolvingAbilityPromptsForLandSource() {
        addReadyRune(player1);
        addReadyLand(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Choosing a land source records a one-shot prevention shield")
    void choosingLandSourceRecordsShield() {
        addReadyRune(player1);
        Permanent land = addReadyLand(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, land.getId());

        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.playerId().equals(player1.getId()) && s.sourceId().equals(land.getId()));
    }

    @Test
    @DisplayName("Prevents the next damage from the chosen land source and consumes the shield")
    void preventsNextDamageFromLandSource() {
        harness.setLife(player1, 20);
        addReadyRune(player1);
        Permanent brushland = addReadyLand(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, brushland.getId());

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Non-land permanents are not valid source choices")
    void nonLandSourceNotValid() {
        addReadyRune(player1);
        addReadyNonLandCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("No permanents on the battlefield"));
    }

    @Test
    @DisplayName("Shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        addReadyRune(player1);
        Permanent land = addReadyLand(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, land.getId());

        assertThat(gd.playerSourceNextDamageShields).isNotEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new RuneOfProtectionLands()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rune of Protection: Lands");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private Permanent addReadyRune(Player player) {
        Permanent perm = new Permanent(new RuneOfProtectionLands());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyLand(Player player) {
        Permanent perm = new Permanent(new Brushland());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyNonLandCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
