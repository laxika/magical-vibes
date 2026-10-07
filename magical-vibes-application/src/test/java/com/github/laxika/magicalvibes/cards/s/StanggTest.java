package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RubiniaSoulsinger;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Stangg.class, RubiniaSoulsinger.class})
class StanggTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates a legendary 3/4 red and green Stangg Twin")
    void enteringCreatesTwin() {
        enterStangg();

        Permanent twin = findPermanent(player1, "Stangg Twin");
        assertThat(twin.getCard().isToken()).isTrue();
        assertThat(twin.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(twin.getCard().getSupertypes()).containsExactly(CardSupertype.LEGENDARY);
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

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, stangg));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Stangg Twin");
        harness.assertInGraveyard(player1, "Stangg");
        assertThat(gameLogContains("Stangg Twin is exiled.")).isTrue();
    }

    @Test
    @DisplayName("When the Twin leaves, Stangg is sacrificed")
    void leavingTwinSacrificesStangg() {
        enterStangg();
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
        Permanent stangg = castStangg();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, stangg));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Stangg");
        assertThat(findPermanents(player1, "Stangg Twin")).hasSize(1);
    }

    @Test
    @DisplayName("The original controller cannot sacrifice Stangg after an opponent gains control of it")
    void stolenStanggIsNotSacrificedWhenTwinLeaves() {
        Permanent stangg = enterStangg();
        Permanent twin = findPermanent(player1, "Stangg Twin");
        Permanent rubinia = addCreatureReady(player2, new RubiniaSoulsinger());
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(rubinia),
                null, stangg.getId());
        resolveAllTriggers();
        harness.assertOnBattlefield(player2, "Stangg");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, twin));
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Stangg");
        harness.assertNotInGraveyard(player1, "Stangg");
    }

    @Test
    @DisplayName("A stolen Twin leaving still triggers the original Stangg controller's delayed ability")
    void stolenTwinDoesNotChangeDelayedTriggerController() {
        enterStangg();
        Permanent twin = findPermanent(player1, "Stangg Twin");
        Permanent rubinia = addCreatureReady(player2, new RubiniaSoulsinger());
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(rubinia),
                null, twin.getId());
        resolveAllTriggers();
        harness.assertOnBattlefield(player2, "Stangg Twin");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, twin));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Stangg");
    }

    @Test
    @DisplayName("A stolen Stangg leaving does not change the controller of the delayed exile trigger")
    void stolenStanggDoesNotChangeDelayedExileTriggerController() {
        Permanent stangg = enterStangg();
        Permanent rubinia = addCreatureReady(player2, new RubiniaSoulsinger());
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(rubinia),
                null, stangg.getId());
        resolveAllTriggers();
        harness.assertOnBattlefield(player2, "Stangg");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, stangg));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Stangg Twin");
    }

    private Permanent enterStangg() {
        Permanent stangg = castStangg();
        harness.passBothPriorities();
        return stangg;
    }

    private Permanent castStangg() {
        harness.castFromHand(player1, new Stangg(), "{4}{R}{G}");
        harness.passBothPriorities();
        return findPermanent(player1, "Stangg");
    }
}
