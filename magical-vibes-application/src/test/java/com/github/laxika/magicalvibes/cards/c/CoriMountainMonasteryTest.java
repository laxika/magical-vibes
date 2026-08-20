package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CoriMountainMonasteryTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you control no Plains or Island")
    void entersTappedWithoutPlainsOrIsland() {
        playLand(new CoriMountainMonastery());

        assertThat(findPermanent(player1, "Cori Mountain Monastery").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control a Plains")
    void entersUntappedWithPlains() {
        harness.addToBattlefield(player1, new Plains());
        playLand(new CoriMountainMonastery());

        assertThat(findPermanent(player1, "Cori Mountain Monastery").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control an Island")
    void entersUntappedWithIsland() {
        harness.addToBattlefield(player1, new Island());
        playLand(new CoriMountainMonastery());

        assertThat(findPermanent(player1, "Cori Mountain Monastery").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping it adds red mana")
    void addsRedMana() {
        Permanent monastery = addReadyMonastery();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(monastery.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Exiles the top card and lets you play it until the end of your next turn")
    void exilesTopCardWithPlayPermission() {
        Card top = new Shock();
        harness.setLibrary(player1, List.of(top));
        addReadyMonastery();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(top);
        assertThat(gd.exilePlayPermissions).containsEntry(top.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd.get(top.getId())).isEqualTo(gd.turnNumber + 2);

        harness.castFromExile(player1, top.getId(), player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    private void playLand(Card land) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(land));
        harness.playLand(player1, 0);
    }

    private Permanent addReadyMonastery() {
        Permanent monastery = new Permanent(new CoriMountainMonastery());
        monastery.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(monastery);
        return monastery;
    }
}
