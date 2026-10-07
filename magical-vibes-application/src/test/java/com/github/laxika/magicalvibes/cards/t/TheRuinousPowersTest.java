package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.Biophagus;
import com.github.laxika.magicalvibes.cards.b.Broodlord;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheRuinousPowers.class, Forest.class, Biophagus.class, Broodlord.class})
class TheRuinousPowersTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a random opponent's top card and lets its controller cast it with any mana")
    void exilesAndCastsRandomOpponentsTopCard() {
        Card topCard = ownedByPlayer2(new Biophagus());
        Permanent source = resolveUpkeepTrigger(topCard);

        assertThat(gd.getCardsExiledByPermanent(source.getId())).containsExactly(topCard);
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
        assertThat(gd.exilePlayAnyManaType).contains(topCard.getId());

        int lifeBefore = gd.getLife(player2.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castFromExile(player1, topCard.getId());

        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - topCard.getManaValue());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Biophagus");
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }

    @Test
    @DisplayName("The exiled land can be played until end of turn")
    void playsExiledLand() {
        Card topCard = ownedByPlayer2(new Forest());
        Permanent source = resolveUpkeepTrigger(topCard);

        assertThat(gd.getCardsExiledByPermanent(source.getId())).containsExactly(topCard);
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayAnyManaType).doesNotContain(topCard.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, topCard.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("A same-named spell not cast from the exiled card does not trigger life loss")
    void doesNotTriggerForAnotherCopy() {
        Card topCard = ownedByPlayer2(new Biophagus());
        resolveUpkeepTrigger(topCard);

        Card handCopy = new Biophagus();
        handCopy.setOwnerId(player1.getId());
        harness.setHand(player1, List.of(handCopy));
        int lifeBefore = gd.getLife(player2.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("The delayed life-loss trigger survives the enchantment leaving the battlefield")
    void lifeLossSurvivesSourceLeavingBattlefield() {
        Card topCard = ownedByPlayer2(new Biophagus());
        Permanent source = resolveUpkeepTrigger(topCard);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, source));

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 2);
        int lifeBefore = gd.getLife(player2.getId());
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Biophagus");
    }

    @Test
    @DisplayName("Life loss includes the chosen X in the spell's mana value")
    void lifeLossIncludesChosenX() {
        Card topCard = ownedByPlayer2(new Broodlord());
        resolveUpkeepTrigger(topCard);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 6);
        int lifeBefore = gd.getLife(player2.getId());

        gs.playCardFromExile(gd, player1, topCard.getId(), 2, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 6);
    }

    @Test
    @DisplayName("An empty opponent library does not exile a card or cause life loss")
    void emptyOpponentLibraryDoesNothing() {
        harness.setLibrary(player2, List.of());
        harness.addToBattlefield(player1, new TheRuinousPowers());
        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An unplayed card stays exiled but its play permission expires at end of turn")
    void playPermissionExpiresAtEndOfTurn() {
        Card topCard = ownedByPlayer2(new Biophagus());
        resolveUpkeepTrigger(topCard);
        harness.setLibrary(player2, List.of(ownedByPlayer2(new Forest())));
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.exilePlayAnyManaType).doesNotContain(topCard.getId());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent resolveUpkeepTrigger(Card topCard) {
        harness.setLibrary(player2, List.of(topCard));
        Permanent source = harness.addToBattlefieldAndReturn(player1, new TheRuinousPowers());
        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        return source;
    }

    private Card ownedByPlayer2(Card card) {
        card.setOwnerId(player2.getId());
        return card;
    }
}
