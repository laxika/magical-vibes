package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StanggTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates a legendary 3/4 red and green Stangg Twin")
    void enteringCreatesTwin() {
        enterStangg();

        Permanent twin = findPermanent(player1, "Stangg Twin");
        assertThat(twin.getCard().isToken()).isTrue();
        assertThat(twin.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(twin.getCard().getPower()).isEqualTo(3);
        assertThat(twin.getCard().getToughness()).isEqualTo(4);
        assertThat(twin.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(twin.getCard().getColors()).containsExactlyInAnyOrder(CardColor.RED, CardColor.GREEN);
        assertThat(twin.getCard().getSubtypes()).containsExactly(CardSubtype.HUMAN, CardSubtype.WARRIOR);
    }

    @Test
    @DisplayName("When Stangg leaves, its Twin is exiled")
    void leavingExilesTwin() {
        Permanent stangg = enterStangg();
        Permanent twin = findPermanent(player1, "Stangg Twin");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, stangg));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Stangg Twin");
        harness.assertInGraveyard(player1, "Stangg");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getId())
                .contains(twin.getCard().getId());
    }

    @Test
    @DisplayName("When the Twin leaves, Stangg is sacrificed")
    void leavingTwinSacrificesStangg() {
        Permanent stangg = enterStangg();
        Permanent twin = findPermanent(player1, "Stangg Twin");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, twin));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Stangg");
        harness.assertInGraveyard(player1, "Stangg");
    }

    @Test
    @DisplayName("A Twin created after Stangg has left remains unlinked")
    void enteringTriggerStillCreatesUnlinkedTwinAfterSourceLeaves() {
        Permanent stangg = harness.addToBattlefieldAndReturn(player1, new Stangg());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, stangg));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Stangg");
        assertThat(findPermanents(player1, "Stangg Twin")).hasSize(1);
    }

    private Permanent enterStangg() {
        Permanent stangg = harness.addToBattlefieldAndReturn(player1, new Stangg());
        harness.passBothPriorities();
        return stangg;
    }
}
