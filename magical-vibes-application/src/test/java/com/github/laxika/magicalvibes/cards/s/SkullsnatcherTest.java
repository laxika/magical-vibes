package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FrostOgre;
import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.t.TendoIceBridge;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Skullsnatcher.class, FrostOgre.class, GnarledMass.class, TendoIceBridge.class})
class SkullsnatcherTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage exiles up to two chosen cards from the damaged player's graveyard")
    void combatDamageExilesTwoChosenCards() {
        Card gnarledMass = new GnarledMass();
        Card frostOgre = new FrostOgre();
        Card land = new TendoIceBridge();
        harness.setGraveyard(player2, List.of(gnarledMass, frostOgre, land));

        attackDealingDamage();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, List.of(gnarledMass.getId(), land.getId()));
        resolveAllTriggers();

        assertThat(graveyardNames(player2.getId())).containsExactly("Frost Ogre");
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Choosing no cards exiles nothing")
    void choosingNoCardsExilesNothing() {
        harness.setGraveyard(player2, List.of(new GnarledMass()));

        attackDealingDamage();

        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(graveyardNames(player2.getId())).containsExactly("Gnarled Mass");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only the damaged player's graveyard is offered")
    void controllerGraveyardIsNotOffered() {
        Card ownCard = new GnarledMass();
        Card damagedPlayerCard = new FrostOgre();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(damagedPlayerCard));

        attackDealingDamage();

        List<UUID> valid = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds();
        assertThat(valid).containsExactly(damagedPlayerCard.getId());
    }

    @Test
    @DisplayName("An empty graveyard presents no choice")
    void emptyGraveyardPresentsNoChoice() {
        harness.setGraveyard(player2, List.of());

        attackDealingDamage();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Ninjutsu returns an unblocked attacker and puts Skullsnatcher onto the battlefield attacking")
    void ninjutsuSwapsTheUnblockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new GnarledMass());
        addCreatureReady(player2, new FrostOgre());
        harness.setGraveyard(player2, List.of(new TendoIceBridge()));
        declareAttackers(List.of(0));
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Skullsnatcher()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Gnarled Mass");
        Permanent skullsnatcher = findPermanent(player1, "Skullsnatcher");
        assertThat(skullsnatcher.isTapped()).isTrue();
        assertThat(skullsnatcher.isAttacking()).isTrue();
        assertThat(skullsnatcher.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Ninjutsu cannot return a blocked attacker")
    void ninjutsuRejectsBlockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new GnarledMass());
        addCreatureReady(player2, new FrostOgre());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Skullsnatcher()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unblocked attacker");
    }

    private List<String> graveyardNames(UUID playerId) {
        return gd.playerGraveyards.get(playerId).stream().map(Card::getName).toList();
    }

    private void attackDealingDamage() {
        addCreatureReady(player1, new Skullsnatcher());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
    }
}
