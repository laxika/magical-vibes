package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BridledBighorn;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.i.InventiveWingsmith;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.TrainedArynx;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShepherdOfTheClouds.class, BridledBighorn.class, GrizzlyBears.class, HolyDay.class, SerraAngel.class,
        InventiveWingsmith.class, Pacifism.class, Plains.class, TrainedArynx.class})
class ShepherdOfTheCloudsTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a permanent card with mana value three or less to hand without a Mount")
    void returnsPermanentToHandWithoutMount() {
        Card returned = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returned));

        castShepherd(returned);

        assertThat(gd.playerHands.get(player1.getId())).contains(returned);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(returned.getId()));
    }

    @Test
    @DisplayName("Returns the target permanent card to the battlefield when controlling a Mount")
    void returnsPermanentToBattlefieldWithMount() {
        harness.addToBattlefield(player1, new BridledBighorn());
        Card returned = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returned));

        castShepherd(returned);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(returned.getId()));
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(returned);
    }

    @Test
    @DisplayName("Cannot target a permanent card with mana value greater than three")
    void cannotTargetHighManaValuePermanent() {
        Card returned = new SerraAngel();
        harness.setGraveyard(player1, List.of(returned));
        harness.castFromHand(player1, new ShepherdOfTheClouds(), "{4}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(returned);
    }

    @Test
    @DisplayName("Cannot target a nonpermanent card")
    void cannotTargetNonpermanentCard() {
        Card returned = new HolyDay();
        harness.setGraveyard(player1, List.of(returned));
        harness.castFromHand(player1, new ShepherdOfTheClouds(), "{4}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(returned);
    }

    @Test
    void opposingMountDoesNotChangeDestination() {
        harness.addToBattlefield(player2, new BridledBighorn());
        Card returned = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returned));

        castShepherd(returned);

        assertThat(gd.playerHands.get(player1.getId())).contains(returned);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void mountEnteringBeforeResolutionChangesDestination() {
        Card returned = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returned));
        chooseShepherdTarget(returned);
        harness.addToBattlefield(player1, new BridledBighorn());

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(returned);
    }

    @Test
    void mountLeavingBeforeResolutionChangesDestination() {
        harness.addToBattlefield(player1, new BridledBighorn());
        Card returned = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returned));
        chooseShepherdTarget(returned);
        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard() instanceof BridledBighorn);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(returned);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void returningAMountWithoutAnotherMountStillReturnsItToHand() {
        Card returned = new TrainedArynx();
        harness.setGraveyard(player1, List.of(returned));

        castShepherd(returned);

        assertThat(gd.playerHands.get(player1.getId())).contains(returned);
        harness.assertNotOnBattlefield(player1, "Trained Arynx");
    }

    @Test
    void returnsManaValueThreePermanent() {
        Card returned = new InventiveWingsmith();
        harness.setGraveyard(player1, List.of(returned));

        castShepherd(returned);

        assertThat(gd.playerHands.get(player1.getId())).contains(returned);
        harness.assertNotInGraveyard(player1, "Inventive Wingsmith");
    }

    @Test
    void returnsLandToBattlefieldWithMount() {
        harness.addToBattlefield(player1, new BridledBighorn());
        Card returned = new Plains();
        harness.setGraveyard(player1, List.of(returned));

        castShepherd(returned);

        harness.assertOnBattlefield(player1, "Plains");
        harness.assertNotInGraveyard(player1, "Plains");
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(returned);
    }

    @Test
    void cannotTargetOpponentsGraveyard() {
        Card returned = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(returned));
        harness.castFromHand(player1, new ShepherdOfTheClouds(), "{4}{W}");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(returned);
    }

    @Test
    void targetLeavingGraveyardIsNotReturned() {
        Card returned = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returned));
        chooseShepherdTarget(returned);
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(returned));

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(returned);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(returned);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void returningAuraAllowsChoosingWhatItEnchants() {
        harness.addToBattlefield(player1, new BridledBighorn());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card returned = new Pacifism();
        harness.setGraveyard(player1, List.of(returned));

        castShepherd(returned);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.assertOnBattlefield(player1, "Pacifism");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(returned.getId())
                        && harness.getPermanentId(player2, "Grizzly Bears").equals(permanent.getAttachedTo()));
    }

    private void castShepherd(Card returned) {
        chooseShepherdTarget(returned);
        harness.passBothPriorities();
    }

    private void chooseShepherdTarget(Card returned) {
        harness.castFromHand(player1, new ShepherdOfTheClouds(), "{4}{W}");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(returned.getId()));
    }

}
