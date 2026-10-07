package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ConniveConcoct;
import com.github.laxika.magicalvibes.cards.d.DimirGuildgate;
import com.github.laxika.magicalvibes.cards.d.DirectCurrent;
import com.github.laxika.magicalvibes.cards.h.HealersHawk;
import com.github.laxika.magicalvibes.cards.s.SelesnyaGuildgate;
import com.github.laxika.magicalvibes.cards.v.VernadiShieldmate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

@CardUsed({ThiefOfSanity.class, DimirGuildgate.class, SelesnyaGuildgate.class,
        VernadiShieldmate.class, DirectCurrent.class, HealersHawk.class, ConniveConcoct.class})
class ThiefOfSanityTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage exiles one of the damaged player's top three cards and puts the rest into their graveyard")
    void combatDamageExilesOneTopCardAndMillsTheRest() {
        Card topCard = new SelesnyaGuildgate();
        Card chosenCard = new DimirGuildgate();
        Card thirdCard = new VernadiShieldmate();
        harness.setLibrary(player2, List.of(topCard, chosenCard, thirdCard));
        Permanent thief = addAttackingThief();

        resolveCombatAndTrigger();

        PendingInteraction.LibrarySearch choice =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(choice).isNotNull();
        assertThat(choice.params().playerId()).isEqualTo(player1.getId());
        assertThat(choice.params().targetPlayerId()).isEqualTo(player2.getId());
        assertThat(choice.params().cards()).containsExactly(topCard, chosenCard, thirdCard);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.getCardsExiledByPermanent(thief.getId())).containsExactly(chosenCard);
        assertThat(gd.exiledCards).filteredOn(entry -> entry.card().getId().equals(chosenCard.getId()))
                .allMatch(ExiledCardEntry::faceDown);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(topCard, thirdCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The exiled card remains castable with any mana after Thief of Sanity leaves")
    void exiledCardCanBeCastAfterThiefLeaves() {
        Card stolenCard = new VernadiShieldmate();
        harness.setLibrary(player2, List.of(stolenCard, new SelesnyaGuildgate(), new DimirGuildgate()));
        Permanent thief = addAttackingThief();
        resolveCombatAndTrigger();

        harness.handleCardChosen(player1, 0);

        harness.setHand(player2, List.of(new DirectCurrent()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player2, 0, thief.getId());
        harness.assertNotOnBattlefield(player1, "Thief of Sanity");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castFromExile(player1, stolenCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vernadi Shieldmate");
    }

    @Test
    @DisplayName("A blocked Thief of Sanity does not trigger")
    void blockedDoesNotTrigger() {
        addAttackingThief();
        Permanent blocker = addCreatureReady(player2, new HealersHawk());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLibrary(player2, List.of(new SelesnyaGuildgate(), new DimirGuildgate(), new VernadiShieldmate()));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Only the top three cards are affected, leaving deeper cards in order")
    void deeperCardsRemainInLibrary() {
        Card chosen = new VernadiShieldmate();
        Card second = new DimirGuildgate();
        Card third = new SelesnyaGuildgate();
        Card fourth = new HealersHawk();
        Card fifth = new DirectCurrent();
        harness.setLibrary(player2, List.of(chosen, second, third, fourth, fifth));
        Permanent thief = addAttackingThief();

        resolveCombatAndTrigger();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getCardsExiledByPermanent(thief.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(second, third);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(fourth, fifth);
    }

    @Test
    @DisplayName("With only two library cards, exile one and put the other into its owner's graveyard")
    void twoCardLibrary() {
        Card chosen = new VernadiShieldmate();
        Card other = new DimirGuildgate();
        harness.setLibrary(player2, List.of(chosen, other));
        Permanent thief = addAttackingThief();

        resolveCombatAndTrigger();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getCardsExiledByPermanent(thief.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(other);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("With only one library card, exile it and put nothing into the graveyard")
    void oneCardLibrary() {
        Card chosen = new VernadiShieldmate();
        harness.setLibrary(player2, List.of(chosen));
        Permanent thief = addAttackingThief();

        resolveCombatAndTrigger();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getCardsExiledByPermanent(thief.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library produces no choice and does not cause a draw loss")
    void emptyLibrary() {
        harness.setLibrary(player2, List.of());
        Permanent thief = addAttackingThief();

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getCardsExiledByPermanent(thief.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("The cast permission does not allow playing an exiled land")
    void cannotPlayExiledLand() {
        Card land = new DimirGuildgate();
        harness.setLibrary(player2, List.of(land, new VernadiShieldmate(), new SelesnyaGuildgate()));
        addAttackingThief();
        resolveCombatAndTrigger();
        harness.handleCardChosen(player1, 0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(land.getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Dimir Guildgate");
    }

    @Test
    @DisplayName("An exiled creature still requires normal timing and payment, and can be cast only once")
    void castPermissionPreservesTimingAndManaCost() {
        Card stolen = new VernadiShieldmate();
        harness.setLibrary(player2, List.of(stolen, new DimirGuildgate(), new SelesnyaGuildgate()));
        addAttackingThief();
        resolveCombatAndTrigger();
        harness.handleCardChosen(player1, 0);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player1, stolen.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(stolen.getId())).isNotNull();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.playerManaPools.get(player1.getId()).clear();
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, stolen.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(stolen.getId())).isNotNull();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, stolen.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Vernadi Shieldmate");
        assertThat(gd.findExiledCard(stolen.getId())).isNull();
        assertThatThrownBy(() -> harness.castFromExile(player1, stolen.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Changing control of Thief does not give its new controller permission to cast an earlier exiled card")
    void originalControllerKeepsCastPermission() {
        Card stolen = new VernadiShieldmate();
        harness.setLibrary(player2, List.of(stolen, new DimirGuildgate(), new SelesnyaGuildgate()));
        Permanent thief = addAttackingThief();
        resolveCombatAndTrigger();
        harness.handleCardChosen(player1, 0);
        stealThief(thief);
        harness.addMana(player2, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castFromExile(player2, stolen.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(stolen.getId())).isNotNull();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castFromExile(player1, stolen.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vernadi Shieldmate");
    }

    @Test
    @DisplayName("Changing control of Thief must not reveal earlier face-down exiled cards to its new controller")
    void newControllerCannotLookAtEarlierExiledCards() throws Exception {
        Card stolen = new DimirGuildgate();
        harness.setLibrary(player2, List.of(stolen, new VernadiShieldmate(), new SelesnyaGuildgate()));
        Permanent thief = addAttackingThief();
        resolveCombatAndTrigger();
        harness.handleCardChosen(player1, 0);
        stealThief(thief);
        harness.publishState();

        var mapper = new JacksonConfig().objectMapper();
        GameStateMessage state = mapper.readValue(harness.getConn2()
                .getMessagesContaining("\"type\":\"GAME_STATE\"").getLast(), GameStateMessage.class);
        var view = state.battlefields().stream().flatMap(List::stream)
                .filter(permanent -> permanent.id().equals(thief.getId())).findFirst().orElseThrow();

        assertThat(view.faceDownExiledCards()).isEmpty();
        assertThat(view.faceDownExiledCount()).isEqualTo(1);
    }

    private void stealThief(Permanent thief) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ConniveConcoct()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castModalSorcery(player2, 0, 0, List.of(thief.getId()));
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(thief);
    }

    private Permanent addAttackingThief() {
        Permanent thief = addCreatureReady(player1, new ThiefOfSanity());
        thief.setAttacking(true);
        return thief;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        resolveAllTriggers();
    }
}
