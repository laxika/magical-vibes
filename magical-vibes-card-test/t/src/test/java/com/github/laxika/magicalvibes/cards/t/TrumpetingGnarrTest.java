package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrumpetingGnarr.class})
class TrumpetingGnarrTest extends BaseCardTest {

    @Test
    @DisplayName("Mutating creates a 3/3 green Beast token")
    void mutatingCreatesBeastToken() {
        Permanent gnarr = addCreatureReady(player1, new TrumpetingGnarr());

        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, gnarr, List.of(gnarr.getCard()), player1.getId()));
        resolveAllTriggers();

        List<Permanent> beasts = findPermanents(player1, "Beast");
        assertThat(beasts).hasSize(1);
        Permanent beast = beasts.getFirst();
        assertThat(beast.getCard().getPower()).isEqualTo(3);
        assertThat(beast.getCard().getToughness()).isEqualTo(3);
        assertThat(beast.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(beast.getCard().getSubtypes()).containsExactly(CardSubtype.BEAST);
        assertThat(beast.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Casting normally does not create a Beast token")
    void normalCastDoesNotCreateToken() {
        harness.castFromHand(player1, new TrumpetingGnarr(), "{1}{G}{U}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Trumpeting Gnarr");
        assertThat(findPermanents(player1, "Beast")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Trumpeting Gnarr can be cast for its mutate cost")
    void canCastForMutateCost() {
        Permanent target = addCreatureReady(player1, new TrumpetingGnarr());
        harness.setHand(player1, List.of(new TrumpetingGnarr()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castWithAlternateCost(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each mutation creates exactly one Beast for the controller")
    void repeatedMutationsCreateOneTokenEachForController() {
        Permanent gnarr = addCreatureReady(player2, new TrumpetingGnarr());

        for (int mutation = 0; mutation < 2; mutation++) {
            harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                    gd, gnarr, List.of(gnarr.getCard()), player2.getId()));
            assertThat(findPermanents(player2, "Beast")).hasSize(mutation);
            resolveAllTriggers();

            assertThat(findPermanents(player2, "Beast")).hasSize(mutation + 1);
            assertThat(findPermanents(player1, "Beast")).isEmpty();
            assertThat(gd.stack).isEmpty();
        }
    }

    @Test
    @DisplayName("Mutating another creature does not trigger an unmerged Gnarr")
    void anotherCreatureMutatingDoesNotTrigger() {
        Permanent gnarr = addCreatureReady(player1, new TrumpetingGnarr());
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, gnarr, List.of(gnarr.getCard()), player1.getId()));
        resolveAllTriggers();
        Permanent beast = findPermanents(player1, "Beast").getFirst();

        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, beast, List.of(beast.getCard()), player1.getId()));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Beast")).containsExactly(beast);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The mutation trigger creates a Beast even after Gnarr leaves")
    void tokenIsCreatedAfterSourceLeaves() {
        Permanent gnarr = addCreatureReady(player1, new TrumpetingGnarr());
        harness.inMutationScope(() -> {
            harness.getTriggerCollectionService().checkMutateTriggers(
                    gd, gnarr, List.of(gnarr.getCard()), player1.getId());
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, gnarr);
        });
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Beast")).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(gnarr.getCard());
        assertThat(gd.stack).isEmpty();
    }
}
