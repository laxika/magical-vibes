package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RassilonTheWarPresident.class, Island.class, ThinkTwice.class})
class RassilonTheWarPresidentTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep loses 2 life and exiles the top card with an indefinite play permission")
    void upkeepExilesTopCardWithIndefinitePlayPermission() {
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new RassilonTheWarPresident());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(topCard.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd).doesNotContainKey(topCard.getId());
    }

    @Test
    @DisplayName("The controller can play the exiled card on a later turn")
    void exiledCardRemainsPlayableOnLaterTurn() {
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new RassilonTheWarPresident());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setLibrary(player2, List.of(new Island(), new Island()));
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, topCard.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(topCard.getId()));
    }

    @Test
    @DisplayName("Noncreature spells cast from exile gain conspire")
    void noncreatureExiledSpellGainsConspire() {
        harness.addToBattlefield(player1, new RassilonTheWarPresident());
        Card exiledSpell = new ThinkTwice();
        gd.addToExile(player1.getId(), exiledSpell);
        gd.exilePlayPermissions.put(exiledSpell.getId(), player1.getId());

        assertThat(gqs.hasSpellCastingAbilityGrant(
                gd, player1.getId(), exiledSpell, Keyword.CONSPIRE, Zone.EXILE)).isTrue();
    }

    @Test
    @DisplayName("An empty library does not prevent the upkeep life loss")
    void emptyLibraryStillLosesLife() {
        harness.setLibrary(player1, List.of());
        harness.addToBattlefield(player1, new RassilonTheWarPresident());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The opponent's upkeep does not trigger Rassilon")
    void opponentsUpkeepDoesNotTrigger() {
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new RassilonTheWarPresident());

        advanceToUpkeep(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The upkeep trigger still resolves after Rassilon leaves the battlefield")
    void upkeepTriggerSurvivesSourceLeaving() {
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new RassilonTheWarPresident());

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
    }

    @Test
    @DisplayName("An exiled spell still requires its normal mana cost and may be cast without conspiring")
    void exiledSpellRequiresManaAndConspireIsOptional() {
        Card spell = new ThinkTwice();
        Card drawnCard = new Island();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(spell, drawnCard));
        harness.addToBattlefield(player1, new RassilonTheWarPresident());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            harness.castFromExile(player1, spell.getId());
            resolveAllTriggers();
        });

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("The exile play permission belongs only to the upkeep ability's controller")
    void opponentCannotUseExilePermission() {
        Card spell = new ThinkTwice();
        harness.setLibrary(player1, List.of(spell, new Island()));
        harness.addToBattlefield(player1, new RassilonTheWarPresident());
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castFromExile(player2, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.exilePlayPermissions).containsEntry(spell.getId(), player1.getId());
    }
}
