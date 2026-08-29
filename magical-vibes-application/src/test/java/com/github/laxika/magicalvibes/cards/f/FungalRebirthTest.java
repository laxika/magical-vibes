package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BloodthroneVampire;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FungalRebirthTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target permanent card from the graveyard to its owner's hand")
    void returnsTargetPermanentCardToHand() {
        Card target = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new FungalRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Leonin Scimitar");
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
    }

    @Test
    @DisplayName("Creates two Saproling tokens if a creature died this turn")
    void createsTwoSaprolingsWithMorbid() {
        Card target = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(target));
        harness.addToBattlefield(player1, new BloodthroneVampire());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FungalRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        java.util.UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Leonin Scimitar");
        assertThat(findPermanents(player1, "Saproling")).hasSize(2);
    }

    @Test
    @DisplayName("Cannot target a nonpermanent card in a graveyard")
    void cannotTargetNonpermanentCard() {
        Card target = new HolyDay();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new FungalRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("permanent");
    }
}
