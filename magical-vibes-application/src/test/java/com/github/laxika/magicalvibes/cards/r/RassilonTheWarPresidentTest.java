package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RassilonTheWarPresident.class, Island.class, GiantGrowth.class})
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
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, topCard.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(topCard.getId()));
    }

    @Test
    @DisplayName("Noncreature spells cast from exile gain conspire")
    void noncreatureExiledSpellGainsConspire() {
        harness.addToBattlefield(player1, new RassilonTheWarPresident());
        Card exiledSpell = new GiantGrowth();
        gd.addToExile(player1.getId(), exiledSpell);
        gd.exilePlayPermissions.put(exiledSpell.getId(), player1.getId());

        assertThat(gqs.hasSpellCastingAbilityGrant(
                gd, player1.getId(), exiledSpell, Keyword.CONSPIRE, Zone.EXILE)).isTrue();
    }
}
