package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlphaKavu;
import com.github.laxika.magicalvibes.cards.a.AuroraGriffin;
import com.github.laxika.magicalvibes.cards.k.KavuRecluse;
import com.github.laxika.magicalvibes.cards.m.MeteorCrater;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SteelLeafPaladin.class, AlphaKavu.class, AuroraGriffin.class, KavuRecluse.class,
        MeteorCrater.class})
class SteelLeafPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("ETB allows choosing a green or white creature you control, including itself")
    void etbOffersGreenOrWhiteCreaturesYouControl() {
        UUID greenId = harness.addToBattlefieldAndReturn(player1, new AlphaKavu()).getId();
        UUID whiteId = harness.addToBattlefieldAndReturn(player1, new AuroraGriffin()).getId();
        UUID redId = harness.addToBattlefieldAndReturn(player1, new KavuRecluse()).getId();
        UUID landId = harness.addToBattlefieldAndReturn(player1, new MeteorCrater()).getId();
        UUID opponentWhiteId = harness.addToBattlefieldAndReturn(player2, new AuroraGriffin()).getId();

        harness.castFromHand(player1, new SteelLeafPaladin(), "{4}{G}{W}");
        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        UUID paladinId = harness.getPermanentId(player1, "Steel Leaf Paladin");
        assertThat(choice.playerId())
                .isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(greenId, whiteId, paladinId);
        assertThat(choice.validIds()).doesNotContain(redId, landId, opponentWhiteId);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.BounceCreature.class);
    }

    @Test
    @DisplayName("The chosen green or white creature returns to its owner's hand")
    void chosenCreatureReturnsToHand() {
        harness.addToBattlefield(player1, new AlphaKavu());
        UUID whiteId = harness.addToBattlefieldAndReturn(player1, new AuroraGriffin()).getId();

        harness.castFromHand(player1, new SteelLeafPaladin(), "{4}{G}{W}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, whiteId);

        harness.assertNotOnBattlefield(player1, "Aurora Griffin");
        harness.assertInHand(player1, "Aurora Griffin");
        harness.assertOnBattlefield(player1, "Alpha Kavu");
        harness.assertOnBattlefield(player1, "Steel Leaf Paladin");
    }

    @Test
    @DisplayName("A chosen green creature returns to its owner's hand")
    void chosenGreenCreatureReturnsToHand() {
        UUID greenId = harness.addToBattlefieldAndReturn(player1, new AlphaKavu()).getId();

        harness.castFromHand(player1, new SteelLeafPaladin(), "{4}{G}{W}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, greenId);

        harness.assertNotOnBattlefield(player1, "Alpha Kavu");
        harness.assertInHand(player1, "Alpha Kavu");
        harness.assertOnBattlefield(player1, "Steel Leaf Paladin");
    }

    @Test
    @DisplayName("The Paladin itself can be returned to its owner's hand")
    void sourceCanBeReturnedToItsOwnersHand() {
        harness.castFromHand(player1, new SteelLeafPaladin(), "{4}{G}{W}");
        resolveAllTriggers();

        UUID paladinId = harness.getPermanentId(player1, "Steel Leaf Paladin");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(paladinId);

        harness.handlePermanentChosen(player1, paladinId);

        harness.assertInHand(player1, "Steel Leaf Paladin");
        harness.assertNotOnBattlefield(player1, "Steel Leaf Paladin");
    }

    @Test
    @DisplayName("A returned creature goes to its owner's hand rather than its controller's hand")
    void returnedCreatureGoesToItsOwnersHand() {
        AlphaKavu ownedByPlayer2 = new AlphaKavu();
        ownedByPlayer2.setOwnerId(player2.getId());
        UUID creatureId = harness.addToBattlefieldAndReturn(player1, ownedByPlayer2).getId();

        harness.castFromHand(player1, new SteelLeafPaladin(), "{4}{G}{W}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, creatureId);

        harness.assertInHand(player2, "Alpha Kavu");
        harness.assertNotInHand(player1, "Alpha Kavu");
        harness.assertOnBattlefield(player1, "Steel Leaf Paladin");
    }
}
