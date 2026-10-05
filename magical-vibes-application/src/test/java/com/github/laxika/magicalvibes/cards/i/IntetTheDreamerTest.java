package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GossamerPhantasm;
import com.github.laxika.magicalvibes.cards.e.Enslave;
import com.github.laxika.magicalvibes.cards.u.UrborgTombOfYawgmoth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.GameStateMessage;
import com.github.laxika.magicalvibes.service.JacksonConfig;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IntetTheDreamer.class, GossamerPhantasm.class, UrborgTombOfYawgmoth.class, Enslave.class})
class IntetTheDreamerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2}{U} exiles the top card of the controller's library face down with Intet")
    void payingExilesTopCardFaceDownWithIntet() {
        Permanent intet = addAttackingIntet();
        Card topCard = new GossamerPhantasm();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombatToMayPrompt();
        payForTrigger();

        assertThat(gd.getCardsExiledByPermanent(intet.getId())).containsExactly(topCard);
        assertThat(gd.findExiledCard(topCard.getId())).extracting(ExiledCardEntry::faceDown)
                .isEqualTo(true);
    }

    @Test
    @DisplayName("The card exiled by Intet can be cast without paying its mana cost")
    void castsExiledCardWithoutPayingManaCost() {
        Permanent intet = addAttackingIntet();
        Card topCard = new GossamerPhantasm();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombatToMayPrompt();
        payForTrigger();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gossamer Phantasm");
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.getCardsExiledByPermanent(intet.getId())).isEmpty();
    }

    @Test
    @DisplayName("The card exiled by Intet can be played as a land without paying its mana cost")
    void playsExiledLandWithoutPayingManaCost() {
        Permanent intet = addAttackingIntet();
        Card topCard = new UrborgTombOfYawgmoth();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombatToMayPrompt();
        payForTrigger();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, topCard.getId());

        harness.assertOnBattlefield(player1, "Urborg, Tomb of Yawgmoth");
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.getCardsExiledByPermanent(intet.getId())).isEmpty();
    }

    @Test
    @DisplayName("The free-play permission ends when Intet leaves the battlefield")
    void freePlayPermissionEndsWhenIntetLeavesBattlefield() {
        Permanent intet = addAttackingIntet();
        Card topCard = new GossamerPhantasm();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombatToMayPrompt();
        payForTrigger();

        intet.setMarkedDamage(6);
        harness.runStateBasedActions();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
    }

    @Test
    @DisplayName("Blocked combat damage does not trigger Intet")
    void blockedCombatDamageDoesNotTriggerIntet() {
        Permanent intet = addAttackingIntet();
        Permanent blocker = addCreatureReady(player2, new GossamerPhantasm());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        Card topCard = new GossamerPhantasm();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombatToMayPrompt();

        assertThat(gd.getCardsExiledByPermanent(intet.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Declining the payment does not exile a card")
    void decliningPaymentDoesNotExile() {
        Permanent intet = addAttackingIntet();
        Card topCard = new GossamerPhantasm();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombatToMayPrompt();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getCardsExiledByPermanent(intet.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void originalControllerCanStillPlayAfterIntetChangesControl() {
        Permanent intet = addAttackingIntet();
        Card topCard = new GossamerPhantasm();
        harness.setLibrary(player1, List.of(topCard));
        resolveCombatToMayPrompt();
        payForTrigger();

        stealIntet(intet);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Gossamer Phantasm");
    }

    @Test
    void newControllerCannotPlayCardsExiledByPreviousController() {
        Permanent intet = addAttackingIntet();
        Card topCard = new GossamerPhantasm();
        harness.setLibrary(player1, List.of(topCard));
        resolveCombatToMayPrompt();
        payForTrigger();

        stealIntet(intet);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.castFromExile(player2, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
    }

    @Test
    void exilingPlayerCanStillLookAfterIntetLeavesBattlefield() {
        Permanent intet = addAttackingIntet();
        Card topCard = new GossamerPhantasm();
        harness.setLibrary(player1, List.of(topCard));
        resolveCombatToMayPrompt();
        payForTrigger();

        intet.setMarkedDamage(6);
        harness.runStateBasedActions();
        harness.publishState();

        GameStateMessage state = new JacksonConfig().objectMapper().readValue(harness.getConn1()
                .getMessagesContaining("\"type\":\"GAME_STATE\"").getLast(), GameStateMessage.class);
        assertThat(state.lookedAtExileCards()).anyMatch(card -> card.id().equals(topCard.getId()));
        GameStateMessage opponentState = new JacksonConfig().objectMapper().readValue(harness.getConn2()
                .getMessagesContaining("\"type\":\"GAME_STATE\"").getLast(), GameStateMessage.class);
        assertThat(opponentState.lookedAtExileCards()).noneMatch(card -> card.id().equals(topCard.getId()));
    }

    @Test
    void payingWithAnEmptyLibraryDoesNotExile() {
        Permanent intet = addAttackingIntet();
        harness.setLibrary(player1, List.of());
        resolveCombatToMayPrompt();
        payForTrigger();

        assertThat(gd.getCardsExiledByPermanent(intet.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Intet, the Dreamer");
    }

    @Test
    void exiledCreatureStillRequiresNormalCastingTiming() {
        addAttackingIntet();
        Card topCard = new GossamerPhantasm();
        harness.setLibrary(player1, List.of(topCard));
        resolveCombatToMayPrompt();
        payForTrigger();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
    }

    @Test
    void changingControlDoesNotTransferPermissionToLook() {
        Permanent intet = addAttackingIntet();
        Card topCard = new GossamerPhantasm();
        harness.setLibrary(player1, List.of(topCard));
        resolveCombatToMayPrompt();
        payForTrigger();
        stealIntet(intet);
        harness.publishState();

        GameStateMessage state = new JacksonConfig().objectMapper().readValue(harness.getConn1()
                .getMessagesContaining("\"type\":\"GAME_STATE\"").getLast(), GameStateMessage.class);
        assertThat(state.battlefields().stream().flatMap(List::stream)
                .flatMap(permanent -> permanent.faceDownExiledCards().stream()).map(card -> card.id()))
                .contains(topCard.getId());
        GameStateMessage opponentState = new JacksonConfig().objectMapper().readValue(harness.getConn2()
                .getMessagesContaining("\"type\":\"GAME_STATE\"").getLast(), GameStateMessage.class);
        assertThat(opponentState.battlefields().stream().flatMap(List::stream)
                .flatMap(permanent -> permanent.faceDownExiledCards().stream()).map(card -> card.id()))
                .doesNotContain(topCard.getId());
    }

    private void stealIntet(Permanent intet) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Enslave()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.castEnchantment(player2, 0, intet.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Intet, the Dreamer");
    }

    private Permanent addAttackingIntet() {
        Permanent intet = addCreatureReady(player1, new IntetTheDreamer());
        intet.setAttacking(true);
        return intet;
    }

    private void resolveCombatToMayPrompt() {
        resolveCombat();
        harness.passBothPriorities();
    }

    private void payForTrigger() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);
    }
}
