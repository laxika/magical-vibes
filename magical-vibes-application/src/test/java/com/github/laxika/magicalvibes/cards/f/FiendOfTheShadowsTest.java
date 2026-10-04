package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.b.BlackCat;
import com.github.laxika.magicalvibes.cards.c.ChosenOfMarkov;
import com.github.laxika.magicalvibes.cards.n.NiblisOfTheMist;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FiendOfTheShadows.class, BlackCat.class, Forest.class, ChosenOfMarkov.class, NiblisOfTheMist.class})
class FiendOfTheShadowsTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player prompts that player to exile a card from their hand")
    void combatDamagePromptsExile() {
        addAttackingFiend(player1);
        harness.setHand(player2, List.of(new BlackCat(), new Forest()));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Damaged player chooses the card to exile and it goes to exile, not graveyard")
    void damagedPlayerExilesChosenCard() {
        addAttackingFiend(player1);
        harness.setHand(player2, List.of(new BlackCat(), new Forest()));

        resolveCombatAndTrigger();
        harness.handleCardChosen(player2, 0); // player2 chooses to exile Black Cat

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Black Cat"));
        harness.assertNotInGraveyard(player2, "Black Cat");
    }

    @Test
    @DisplayName("Fiend's controller gains permission to play the exiled card")
    void controllerGainsPlayPermission() {
        addAttackingFiend(player1);
        harness.setHand(player2, List.of(new BlackCat()));

        resolveCombatAndTrigger();
        harness.handleCardChosen(player2, 0);

        Card exiled = gd.getPlayerExiledCards(player2.getId()).stream()
                .filter(c -> c.getName().equals("Black Cat")).findFirst().orElseThrow();
        assertThat(gd.exilePlayPermissions.get(exiled.getId())).isEqualTo(player1.getId());
        // "for as long as it remains exiled" — not impulse, so it must not expire end of turn
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(exiled.getId());
    }

    @Test
    @DisplayName("Controller can play the exiled card even after Fiend of the Shadows leaves the battlefield")
    void permissionPersistsAfterFiendLeaves() {
        addAttackingFiend(player1);
        harness.setHand(player2, List.of(new BlackCat()));

        resolveCombatAndTrigger();
        harness.handleCardChosen(player2, 0);

        Card exiled = gd.getPlayerExiledCards(player2.getId()).stream()
                .filter(c -> c.getName().equals("Black Cat")).findFirst().orElseThrow();

        // Fiend leaves the battlefield — permission persists for as long as the card is exiled
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Fiend of the Shadows"));

        assertThat(gd.exilePlayPermissions.get(exiled.getId())).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Controller can actually play the exiled card from exile under their control")
    void controllerPlaysExiledCard() {
        addAttackingFiend(player1);
        harness.setHand(player2, List.of(new BlackCat()));

        resolveCombatAndTrigger();
        harness.handleCardChosen(player2, 0);

        Card exiled = gd.getPlayerExiledCards(player2.getId()).stream()
                .filter(c -> c.getName().equals("Black Cat")).findFirst().orElseThrow();

        // Move to the controller's postcombat main phase with an empty stack to cast the creature
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castFromExile(player1, exiled.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Black Cat");
        assertThat(gd.findExiledCard(exiled.getId())).isNull();
    }

    @Test
    @DisplayName("No prompt when the damaged player's hand is empty")
    void noPromptWhenHandEmpty() {
        addAttackingFiend(player1);
        harness.setHand(player2, List.of());

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("no cards to exile")).isTrue();
    }

    @Test
    @DisplayName("No trigger when Fiend is blocked and deals no combat damage to a player")
    void noTriggerWhenBlocked() {
        addAttackingFiend(player1);
        Permanent blocker = addCreatureReady(player2, new NiblisOfTheMist());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setHand(player2, List.of(new Forest()));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ExileFromHandChoice.class)).isNull();
    }

    @Test
    @DisplayName("Sacrificing a Human grants a regeneration shield to Fiend of the Shadows")
    void sacrificeHumanRegenerates() {
        Permanent fiend = addCreatureReady(player1, new FiendOfTheShadows());
        harness.addToBattlefield(player1, new ChosenOfMarkov());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chosen of Markov");
        assertThat(fiend.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void exiledSpellStillRequiresMana() {
        addAttackingFiend(player1);
        harness.setHand(player2, List.of(new BlackCat()));
        resolveCombatAndTrigger();
        harness.handleCardChosen(player2, 0);
        Card exiled = gd.getPlayerExiledCards(player2.getId()).getFirst();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromExile(player1, exiled.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(exiled.getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Black Cat");
    }

    @Test
    void exiledCreatureCannotBeCastDuringCombat() {
        addAttackingFiend(player1);
        harness.setHand(player2, List.of(new BlackCat()));
        resolveCombatAndTrigger();
        harness.handleCardChosen(player2, 0);
        Card exiled = gd.getPlayerExiledCards(player2.getId()).getFirst();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, exiled.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(exiled.getId())).isNotNull();
    }

    @Test
    void exiledCardCanBeCastAfterSourceLeavesBeforeTriggerResolves() {
        addAttackingFiend(player1);
        harness.setHand(player2, List.of(new BlackCat()));
        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        Card exiled = gd.getPlayerExiledCards(player2.getId()).getFirst();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castFromExile(player1, exiled.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Black Cat");
        assertThat(gd.findExiledCard(exiled.getId())).isNull();
    }

    @Test
    void exiledLandCanBePlayedAsNormalLandPlay() {
        addAttackingFiend(player1);
        harness.setHand(player2, List.of(new Forest()));
        resolveCombatAndTrigger();
        harness.handleCardChosen(player2, 0);
        Card exiled = gd.getPlayerExiledCards(player2.getId()).getFirst();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.castFromExile(player1, exiled.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.findExiledCard(exiled.getId())).isNull();
        harness.setHand(player1, List.of(new Forest()));
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void regenerationCannotSacrificeNonHumanOrOpponentsHuman() {
        Permanent fiend = addCreatureReady(player1, new FiendOfTheShadows());
        harness.addToBattlefield(player1, new BlackCat());
        harness.addToBattlefield(player2, new ChosenOfMarkov());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Black Cat");
        harness.assertOnBattlefield(player2, "Chosen of Markov");
        assertThat(fiend.getRegenerationShield()).isZero();
    }

    @Test
    void tappedSummoningSickFiendCanRegenerateUsingTappedHuman() {
        Permanent fiend = harness.addToBattlefieldAndReturn(player1, new FiendOfTheShadows());
        fiend.setTapped(true);
        Permanent human = harness.addToBattlefieldAndReturn(player1, new ChosenOfMarkov());
        human.setTapped(true);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Chosen of Markov");
        assertThat(fiend.getRegenerationShield()).isZero();
        harness.passBothPriorities();
        assertThat(fiend.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void regenerationSavesFiendFromLethalCombatDamage() {
        Permanent fiend = addCreatureReady(player1, new FiendOfTheShadows());
        harness.addToBattlefield(player1, new ChosenOfMarkov());
        Permanent blocker = addCreatureReady(player2, new FiendOfTheShadows());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        fiend.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        resolveCombatAndTrigger();

        harness.assertOnBattlefield(player1, "Fiend of the Shadows");
        harness.assertInGraveyard(player2, "Fiend of the Shadows");
        assertThat(fiend.getRegenerationShield()).isZero();
        assertThat(fiend.isTapped()).isTrue();
        assertThat(fiend.isAttacking()).isFalse();
    }

    private Permanent addAttackingFiend(Player player) {
        Permanent fiend = addCreatureReady(player, new FiendOfTheShadows());
        fiend.setAttacking(true);
        return fiend;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities(); // resolve what combat damage triggered
    }

}
