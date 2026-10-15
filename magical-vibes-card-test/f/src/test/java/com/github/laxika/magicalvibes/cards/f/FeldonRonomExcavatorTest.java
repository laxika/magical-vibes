package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FeldonRonomExcavator.class, RagingGoblin.class, Shock.class})
class FeldonRonomExcavatorTest extends BaseCardTest {

    @Test
    void damageExilesThatManyCardsAndLetsControllerChooseOneToPlay() {
        List<Card> topCards = List.of(new RagingGoblin(), new RagingGoblin(), new RagingGoblin());
        gd.playerDecks.get(player1.getId()).addAll(0, topCards);
        harness.addToBattlefield(player1, new FeldonRonomExcavator());
        UUID feldonId = harness.getPermanentId(player1, "Feldon, Ronom Excavator");

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, feldonId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExiledCardMayPlayChoice.class);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(topCards.get(0).getId(), topCards.get(1).getId());

        harness.handleMultipleCardsChosen(player1, List.of(topCards.get(1).getId()));

        assertThat(gd.exilePlayPermissions)
                .containsEntry(topCards.get(1).getId(), player1.getId())
                .doesNotContainKey(topCards.get(0).getId());
        assertThat(gd.playerDecks.get(player1.getId())).first().isEqualTo(topCards.get(2));
    }

    @Test
    void cannotBlock() {
        var attacker = addCreatureReady(player1, new RagingGoblin());
        addCreatureReady(player2, new FeldonRonomExcavator());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void hasteAllowsAttackingImmediately() {
        harness.addToBattlefield(player1, new FeldonRonomExcavator());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(findPermanent(player1, "Feldon, Ronom Excavator").isAttacking()).isTrue();
    }

    @Test
    void lethalDamageStillExilesAvailableCardsFromShortLibrary() {
        Card topCard = new RagingGoblin();
        harness.setLibrary(player1, List.of(topCard));
        dealLethalDamageAndResolveTrigger();

        harness.assertInGraveyard(player1, "Feldon, Ronom Excavator");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        harness.handleMultipleCardsChosen(player1, List.of(topCard.getId()));
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
    }

    @Test
    void emptyLibraryDoesNotPromptForChoice() {
        harness.setLibrary(player1, List.of());
        dealLethalDamageAndResolveTrigger();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Feldon, Ronom Excavator");
    }

    @Test
    void choiceIsMandatoryAndOnlyChosenCardCanBeCastWithNormalManaCost() {
        Card chosen = new RagingGoblin();
        Card other = new RagingGoblin();
        harness.setLibrary(player1, List.of(chosen, other));
        dealLethalDamageAndResolveTrigger();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.castFromExile(player1, chosen.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, other.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.castFromExile(player1, chosen.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Raging Goblin");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(other);
    }

    private void dealLethalDamageAndResolveTrigger() {
        harness.addToBattlefield(player1, new FeldonRonomExcavator());
        UUID feldonId = harness.getPermanentId(player1, "Feldon, Ronom Excavator");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, feldonId);
        harness.passBothPriorities();
    }

    @Test
    void combatDamageExilesCardsWithoutRequiringFeldonToDie() {
        Card topCard = new FeldonRonomExcavator();
        harness.setLibrary(player1, List.of(topCard));
        addCreatureReady(player1, new FeldonRonomExcavator());
        addCreatureReady(player2, new RagingGoblin());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        harness.handleMultipleCardsChosen(player1, List.of(topCard.getId()));
        harness.assertOnBattlefield(player1, "Feldon, Ronom Excavator");
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
    }

    @Test
    void permissionLastsThroughNextTurnAndThenExpiresWithoutRemovingCardFromExile() {
        Card chosen = new RagingGoblin();
        Card other = new RagingGoblin();
        gd.playerDecks.get(player1.getId()).addAll(0, List.of(chosen, other));
        dealLethalDamageAndResolveTrigger();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(chosen.getId(), player1.getId());
        harness.ensurePriority(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, chosen.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(chosen.getId(), player1.getId());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(chosen.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(chosen, other);
    }
}
