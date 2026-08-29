package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RadiantScrollwielderTest extends BaseCardTest {

    @Test
    @DisplayName("Instant and sorcery spells you control have lifelink")
    void controllerInstantHasLifelink() {
        harness.addToBattlefield(player1, new RadiantScrollwielder());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Upkeep exiles a random instant or sorcery from your graveyard and grants cast permission")
    void upkeepExilesMatchingGraveyardCard() {
        harness.addToBattlefield(player1, new RadiantScrollwielder());
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), shock));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
        assertThat(gd.findExiledCard(shock.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(shock.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(shock.getId());
        assertThat(gd.exileInsteadOfGraveyard).contains(shock.getId());
    }

    @Test
    @DisplayName("A spell cast from the exiled card is exiled instead of entering a graveyard")
    void castSpellFromExileRemainsExiled() {
        harness.addToBattlefield(player1, new RadiantScrollwielder());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, shock.getId(), bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.findExiledCard(shock.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(shock);
    }

    @Test
    @DisplayName("Upkeep ability does nothing when your graveyard has no instant or sorcery")
    void upkeepWithNoMatchingCardDoesNothing() {
        harness.addToBattlefield(player1, new RadiantScrollwielder());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(bears.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears);
    }
}
