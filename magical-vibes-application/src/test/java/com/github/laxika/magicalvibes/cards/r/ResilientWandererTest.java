package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DwarvenGrunt;
import com.github.laxika.magicalvibes.cards.f.FlameBurst;
import com.github.laxika.magicalvibes.cards.f.FreneticOgre;
import com.github.laxika.magicalvibes.cards.k.KrosanAvenger;
import com.github.laxika.magicalvibes.model.CardColor;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ResilientWanderer.class, DwarvenGrunt.class, FlameBurst.class, FreneticOgre.class,
        KrosanAvenger.class})
class ResilientWandererTest extends BaseCardTest {

    @Test
    @DisplayName("Discard a card: gains protection from the chosen color until end of turn")
    void grantsProtectionFromChosenColor() {
        Permanent wanderer = addCreatureReady(player1, new ResilientWanderer());
        harness.setHand(player1, List.of(new DwarvenGrunt()));

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);

        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        assertThat(wanderer.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
        harness.assertInGraveyard(player1, "Dwarven Grunt");
    }

    @Test
    @DisplayName("First strike defeats a 3/1 blocker before it deals combat damage")
    void firstStrikeDealsCombatDamageFirst() {
        Permanent wanderer = addCreatureReady(player1, new ResilientWanderer());
        wanderer.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new KrosanAvenger());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Resilient Wanderer");
        harness.assertInGraveyard(player2, "Krosan Avenger");
    }

    @Test
    @DisplayName("Chosen-color protection stops a spell of that color from targeting it")
    void protectionStopsRedRemoval() {
        Permanent wanderer = addCreatureReady(player1, new ResilientWanderer());
        harness.setHand(player1, List.of(new DwarvenGrunt()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        harness.setHand(player2, List.of(new FlameBurst()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, wanderer.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Chosen-color protection prevents combat damage from a source of that color")
    void protectionPreventsRedCombatDamage() {
        Permanent wanderer = addCreatureReady(player1, new ResilientWanderer());
        harness.setHand(player1, List.of(new DwarvenGrunt()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        Permanent attacker = addCreatureReady(player2, new FreneticOgre());
        attacker.setAttacking(true);
        wanderer.setBlocking(true);
        wanderer.addBlockingTarget(0);

        resolveCombat(player2);

        assertThat(wanderer.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Resilient Wanderer");
        harness.assertOnBattlefield(player2, "Frenetic Ogre");
    }

    @Test
    @DisplayName("Protection wears off at end of turn")
    void protectionWearsOffAtEndOfTurn() {
        Permanent wanderer = addCreatureReady(player1, new ResilientWanderer());
        harness.setHand(player1, List.of(new DwarvenGrunt()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");
        assertThat(wanderer.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLUE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wanderer.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.BLUE);
    }

    @Test
    @DisplayName("Cannot activate without a card in hand to discard")
    void cannotActivateWithoutCardToDiscard() {
        addCreatureReady(player1, new ResilientWanderer());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
