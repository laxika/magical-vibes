package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.cards.f.FortressCrab;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.u.UnrulyMob;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({LilianaOfTheVeil.class, WalkingCorpse.class, FortressCrab.class, Plains.class, Swamp.class, UnrulyMob.class})
class LilianaOfTheVeilTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts planeswalker spell on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new LilianaOfTheVeil()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castPlaneswalker(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.PLANESWALKER_SPELL);
        assertThat(entry.getCard()).isInstanceOf(LilianaOfTheVeil.class);
    }

    @Test
    @DisplayName("Resolving puts planeswalker on the battlefield")
    void resolvingEntersBattlefield() {
        harness.setHand(player1, List.of(new LilianaOfTheVeil()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Liliana of the Veil");
    }

    @Test
    @DisplayName("+1 ability makes each player discard a card and increases loyalty")
    void plusOneEachPlayerDiscards() {
        Permanent liliana = addReadyLiliana(player1);

        // Give both players a card in hand
        harness.setHand(player1, List.of(new Swamp()));
        harness.setHand(player2, List.of(new Plains()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(4); // 3 + 1

        // Active player (player1) discards first — enters discard choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);
        harness.assertInGraveyard(player1, "Swamp");
        harness.assertInGraveyard(player2, "Plains");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("-2 ability forces target player to sacrifice a creature")
    void minusTwoTargetPlayerSacrificesCreature() {
        Permanent liliana = addReadyLiliana(player1);
        liliana.setCounterCount(CounterType.LOYALTY, 5);

        harness.addToBattlefield(player2, new WalkingCorpse());

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(3); // 5 - 2
        // With one creature, it's auto-sacrificed
        harness.assertNotOnBattlefield(player2, "Walking Corpse");
        harness.assertInGraveyard(player2, "Walking Corpse");
    }

    @Test
    @DisplayName("-2 ability with multiple creatures prompts choice")
    void minusTwoWithMultipleCreaturesPromptsChoice() {
        Permanent liliana = addReadyLiliana(player1);
        liliana.setCounterCount(CounterType.LOYALTY, 5);

        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.addToBattlefield(player2, new FortressCrab());

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.permanentChoiceContext()).isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);
        harness.handlePermanentChosen(player2, harness.getPermanentId(player2, "Fortress Crab"));
        harness.assertInGraveyard(player2, "Fortress Crab");
        harness.assertOnBattlefield(player2, "Walking Corpse");
    }

    @Test
    @DisplayName("-2 ability has no effect when target has no creatures")
    void minusTwoNoEffectWhenNoCreatures() {
        Permanent liliana = addReadyLiliana(player1);
        liliana.setCounterCount(CounterType.LOYALTY, 5);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(3); // 5 - 2
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no creatures to sacrifice"));
    }

    @Test
    @DisplayName("-6 ability prompts controller to separate permanents into two piles")
    void minusSixPromptsPileSeparation() {
        Permanent liliana = addReadyLiliana(player1);
        liliana.setCounterCount(CounterType.LOYALTY, 6);

        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.addToBattlefield(player2, new FortressCrab());

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(0); // 6 - 6
        // Controller should be prompted to choose permanents for pile 1
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isTrue();
    }

    @Test
    @DisplayName("-6 ability: target player sacrifices chosen pile 1")
    void minusSixTargetPlayerSacrificesPile1() {
        Permanent liliana = addReadyLiliana(player1);
        liliana.setCounterCount(CounterType.LOYALTY, 6);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.addToBattlefield(player2, new FortressCrab());

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        // Step 1: Controller (player1) assigns bears to pile 1, spider to pile 2
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        GameData gd = harness.getGameData();
        // Step 2: Target player (player2) should be prompted to choose a pile
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        // Target player chooses Yes = sacrifice pile 1 (bears)
        harness.handleMayAbilityChosen(player2, true);

        harness.assertNotOnBattlefield(player2, "Walking Corpse");
        harness.assertOnBattlefield(player2, "Fortress Crab");
        harness.assertInGraveyard(player2, "Walking Corpse");
    }

    @Test
    @DisplayName("-6 ability: target player sacrifices chosen pile 2")
    void minusSixTargetPlayerSacrificesPile2() {
        Permanent liliana = addReadyLiliana(player1);
        liliana.setCounterCount(CounterType.LOYALTY, 6);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.addToBattlefield(player2, new FortressCrab());

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        // Step 1: Controller assigns bears to pile 1, spider to pile 2
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        // Step 2: Target player chooses No = sacrifice pile 2 (spider)
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player2, "Walking Corpse");
        harness.assertNotOnBattlefield(player2, "Fortress Crab");
        harness.assertInGraveyard(player2, "Fortress Crab");
    }

    @Test
    @DisplayName("-6 ability: all permanents in one pile, empty other pile")
    void minusSixAllInOnePile() {
        Permanent liliana = addReadyLiliana(player1);
        liliana.setCounterCount(CounterType.LOYALTY, 6);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new FortressCrab());

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        // Controller puts everything in pile 1 (pile 2 is empty)
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId(), spider.getId()));

        // Target player chooses No = sacrifice pile 2 (empty)
        harness.handleMayAbilityChosen(player2, false);

        // Both permanents should survive since pile 2 was empty
        harness.assertOnBattlefield(player2, "Walking Corpse");
        harness.assertOnBattlefield(player2, "Fortress Crab");
    }

    @Test
    @DisplayName("-6 ability: no effect when target has no permanents")
    void minusSixNoEffectWhenNoPermanents() {
        Permanent liliana = addReadyLiliana(player1);
        liliana.setCounterCount(CounterType.LOYALTY, 6);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no permanents to separate"));
    }

    @Test
    @DisplayName("Cannot activate -6 when loyalty is only 3")
    void cannotActivateMinusSixWithInsufficientLoyalty() {
        addReadyLiliana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    @Test
    @DisplayName("Cannot activate loyalty ability during opponent's turn")
    void cannotActivateOnOpponentsTurn() {
        addReadyLiliana(player1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your turn");
    }

    @Test
    @DisplayName("Cannot activate two loyalty abilities on same planeswalker in one turn")
    void cannotActivateTwicePerTurn() {
        addReadyLiliana(player1);
        harness.setHand(player1, List.of(new Swamp()));
        harness.setHand(player2, List.of(new Plains()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        // Complete the discard interactions
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one loyalty ability");
    }

    @Test
    @DisplayName("+1 keeps chosen cards in hand until every player has chosen")
    void plusOneDiscardsSimultaneously() {
        addReadyLiliana(player1);
        harness.setHand(player1, List.of(new Swamp(), new WalkingCorpse()));
        harness.setHand(player2, List.of(new Plains(), new FortressCrab()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        harness.assertInHand(player1, "Walking Corpse");
        harness.assertNotInGraveyard(player1, "Walking Corpse");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player1, "Walking Corpse");
        harness.assertInGraveyard(player2, "Plains");
        harness.assertInHand(player1, "Swamp");
        harness.assertInHand(player2, "Fortress Crab");
    }

    @Test
    @DisplayName("+1 still discards the opponent's card when the controller has an empty hand")
    void plusOneWithControllerEmptyHand() {
        Permanent liliana = addReadyLiliana(player1);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Plains()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Plains");
        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("+1 can resolve when both players have empty hands")
    void plusOneWithBothHandsEmpty() {
        Permanent liliana = addReadyLiliana(player1);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("-2 may target the controller and leaves lands alone")
    void minusTwoCanTargetController() {
        addReadyLiliana(player1);
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player1, new Swamp());

        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Walking Corpse");
        harness.assertOnBattlefield(player1, "Swamp");
        harness.assertOnBattlefield(player1, "Liliana of the Veil");
    }

    @Test
    @DisplayName("-6 includes lands and the source when targeting the controller")
    void minusSixCanSacrificeSourceAndLand() {
        Permanent liliana = addReadyLiliana(player1);
        liliana.setCounterCount(CounterType.LOYALTY, 7);
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.addToBattlefield(player1, new WalkingCorpse());

        harness.activateAbility(player1, 0, 2, null, player1.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(liliana.getId(), swamp.getId()));
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Liliana of the Veil");
        harness.assertInGraveyard(player1, "Swamp");
        harness.assertOnBattlefield(player1, "Walking Corpse");
    }

    @Test
    @DisplayName("-6 creatures dying in the same pile see one another die")
    void minusSixPreservesSimultaneousDeathTriggers() {
        Permanent liliana = addReadyLiliana(player1);
        liliana.setCounterCount(CounterType.LOYALTY, 7);
        Permanent mob = harness.addToBattlefieldAndReturn(player2, new UnrulyMob());
        Permanent corpse = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(mob.getId(), corpse.getId()));
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player2, "Unruly Mob");
        harness.assertInGraveyard(player2, "Walking Corpse");
        assertThat(gd.stack).anySatisfy(entry -> {
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getCard()).isInstanceOf(UnrulyMob.class);
        });
    }

    private Permanent addReadyLiliana(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new LilianaOfTheVeil());
        perm.setCounterCount(CounterType.LOYALTY, 3);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
