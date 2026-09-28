package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SavvyTrader.class, GrizzlyBears.class, Shock.class, Island.class})
class SavvyTraderTest extends BaseCardTest {

    private void castSavvyTrader() {
        harness.setHand(player1, List.of(new SavvyTrader()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB offers only permanent cards from your graveyard")
    void etbOffersOnlyOwnPermanentCards() {
        GrizzlyBears permanent = new GrizzlyBears();
        Island land = new Island();
        harness.setGraveyard(player1, List.of(permanent, new Shock(), land));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        castSavvyTrader();

        List<UUID> validIds = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds();
        assertThat(validIds).containsExactlyInAnyOrder(permanent.getId(), land.getId());
    }

    @Test
    @DisplayName("Exiles the chosen permanent and lets you play it while it remains exiled")
    void exilesPermanentAndGrantsPersistentPlayPermission() {
        GrizzlyBears permanent = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(permanent));
        castSavvyTrader();

        harness.handleMultipleCardsChosen(player1, List.of(permanent.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(permanent);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(permanent);
        assertThat(gd.exilePlayPermissions).containsEntry(permanent.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd).doesNotContainKey(permanent.getId());

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromExile(player1, permanent.getId());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The cost reduction applies to non-hand spells but not hand casts")
    void reducesOnlyNonHandSpellCosts() {
        harness.addToBattlefield(player1, new SavvyTrader());
        GrizzlyBears handCreature = new GrizzlyBears();
        harness.setHand(player1, List.of(handCreature));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
