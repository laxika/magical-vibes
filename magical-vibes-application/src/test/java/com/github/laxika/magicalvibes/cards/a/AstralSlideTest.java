package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.Blackmail;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.f.ForgottenCave;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AstralSlide.class, Blackmail.class, ElvishWarrior.class, ForgottenCave.class})
class AstralSlideTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling a card offers to exile a target creature and returns it at the next end step")
    void cyclingExilesAndReturnsTargetCreature() {
        harness.addToBattlefield(player1, new AstralSlide());
        harness.addToBattlefield(player2, new ElvishWarrior());
        harness.setHand(player1, List.of(new ForgottenCave()));
        harness.setLibrary(player1, List.of(new ElvishWarrior()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Elvish Warrior");
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Elvish Warrior"));
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).hasSize(1);

        resolveAllTriggers();
        advanceToEndStep(player1);

        harness.assertOnBattlefield(player2, "Elvish Warrior");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Elvish Warrior"));
    }

    @Test
    @DisplayName("Cycling by an opponent also triggers Astral Slide")
    void opponentCyclingTriggersSlide() {
        harness.addToBattlefield(player1, new AstralSlide());
        harness.addToBattlefield(player1, new ElvishWarrior());
        harness.setHand(player2, List.of(new ForgottenCave()));
        harness.setLibrary(player2, List.of(new ElvishWarrior()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Elvish Warrior");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateHandAbility(player2, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Elvish Warrior"));
    }

    @Test
    @DisplayName("Declining the may ability leaves the target creature on the battlefield")
    void decliningMayAbilityLeavesTargetCreature() {
        harness.addToBattlefield(player1, new AstralSlide());
        harness.addToBattlefield(player2, new ElvishWarrior());
        harness.setHand(player1, List.of(new ForgottenCave()));
        harness.setLibrary(player1, List.of(new ElvishWarrior()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Elvish Warrior"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Elvish Warrior");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Elvish Warrior"));
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).isEmpty();
    }

    @Test
    @DisplayName("The trigger cannot target a noncreature permanent")
    void triggerCannotTargetNoncreature() {
        harness.addToBattlefield(player1, new AstralSlide());
        harness.addToBattlefield(player2, new ElvishWarrior());
        harness.setHand(player1, List.of(new ForgottenCave()));
        harness.setLibrary(player1, List.of(new ElvishWarrior()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID nonCreatureId = harness.getPermanentId(player1, "Astral Slide");
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonCreatureId))
                .isInstanceOf(IllegalStateException.class);

        UUID creatureId = harness.getPermanentId(player2, "Elvish Warrior");
        harness.handlePermanentChosen(player1, creatureId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("An ordinary discard does not trigger Astral Slide")
    void ordinaryDiscardDoesNotTriggerSlide() {
        harness.addToBattlefield(player1, new AstralSlide());
        harness.addToBattlefield(player2, new ElvishWarrior());
        harness.setHand(player1, List.of(new Blackmail()));
        harness.setHand(player2, List.of(new ElvishWarrior(), new ElvishWarrior(), new ElvishWarrior()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Elvish Warrior");
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
