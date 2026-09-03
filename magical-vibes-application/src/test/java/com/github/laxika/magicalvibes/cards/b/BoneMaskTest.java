package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.EnergyBolt;
import com.github.laxika.magicalvibes.cards.g.GiantMantis;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoneMask.class, GiantMantis.class, EnergyBolt.class})
class BoneMaskTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability prompts for a source choice")
    void activatingPromptsForSourceChoice() {
        harness.addToBattlefieldAndReturn(player1, new BoneMask());
        addCreatureReady(player2, new GiantMantis());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Prevents the chosen source's next damage and exiles that many cards from the library")
    void preventsDamageAndExilesCards() {
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new GiantMantis(), new GiantMantis(), new GiantMantis()));
        harness.addToBattlefieldAndReturn(player1, new BoneMask());
        Permanent mantis = addCreatureReady(player2, new GiantMantis());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, mantis.getId());

        mantis.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        // Giant Mantis is 2/4 — two cards exiled, one left in the library
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.exiledCards).hasSize(2);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Exiles only as many cards as the library holds")
    void exilesNoMoreThanTheLibraryHolds() {
        harness.setLibrary(player1, List.of(new GiantMantis()));
        harness.addToBattlefieldAndReturn(player1, new BoneMask());
        Permanent mantis = addCreatureReady(player2, new GiantMantis());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, mantis.getId());

        mantis.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).hasSize(1);
    }

    @Test
    @DisplayName("A different source still deals damage and exiles nothing")
    void differentSourceStillDealsDamage() {
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new GiantMantis(), new GiantMantis()));
        harness.addToBattlefieldAndReturn(player1, new BoneMask());
        Permanent chosen = addCreatureReady(player2, new GiantMantis());
        Permanent other = addCreatureReady(player2, new GiantMantis());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        other.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 18);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.sourceId().equals(chosen.getId()));
    }

    @Test
    @DisplayName("Shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        harness.addToBattlefieldAndReturn(player1, new BoneMask());
        Permanent mantis = addCreatureReady(player2, new GiantMantis());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, mantis.getId());

        assertThat(gd.playerSourceNextDamageShields).isNotEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Prevents damage from a chosen spell on the stack and exiles that many cards")
    void preventsDamageFromChosenSpellOnStack() {
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new GiantMantis(), new GiantMantis(), new GiantMantis()));
        harness.addToBattlefieldAndReturn(player1, new BoneMask());

        EnergyBolt bolt = new EnergyBolt();
        harness.setHand(player2, List.of(bolt));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castModalSorceryWithModesForX(player2, 0, 1, new int[]{0}, 2, player1.getId(), List.of());

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bolt.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.exiledCards).hasSize(2);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }
}
