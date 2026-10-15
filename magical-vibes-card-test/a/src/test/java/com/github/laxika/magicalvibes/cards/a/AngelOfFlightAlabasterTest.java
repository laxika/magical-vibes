package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ChapelGeist;
import com.github.laxika.magicalvibes.cards.v.VoicelessSpirit;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AngelOfFlightAlabaster.class, VoicelessSpirit.class, ChapelGeist.class, AbbeyGriffin.class})
class AngelOfFlightAlabasterTest extends BaseCardTest {

    @Test
    @DisplayName("Controller's upkeep targets a Spirit before returning it to hand")
    void upkeepReturnsSpiritFromGraveyardToHand() {
        VoicelessSpirit spirit = new VoicelessSpirit();
        harness.addToBattlefield(player1, new AngelOfFlightAlabaster());
        harness.setGraveyard(player1, List.of(spirit));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        harness.assertInGraveyard(player1, "Voiceless Spirit");

        harness.passBothPriorities();

        harness.assertInHand(player1, "Voiceless Spirit");
        harness.assertNotInGraveyard(player1, "Voiceless Spirit");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Returns the selected Spirit when multiple Spirits are in the graveyard")
    void returnsSpecificSpiritFromGraveyard() {
        ChapelGeist spirit = new ChapelGeist();
        harness.addToBattlefield(player1, new AngelOfFlightAlabaster());
        harness.setGraveyard(player1, List.of(new VoicelessSpirit(), spirit));

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Chapel Geist");
        harness.assertInGraveyard(player1, "Voiceless Spirit");
        harness.assertNotInGraveyard(player1, "Chapel Geist");
    }

    @Test
    @DisplayName("No ability remains on the stack when the graveyard is empty")
    void noEffectWithEmptyGraveyard() {
        harness.addToBattlefield(player1, new AngelOfFlightAlabaster());
        harness.setGraveyard(player1, List.of());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No ability remains on the stack when there are no Spirit cards to target")
    void noEffectWithOnlyNonSpiritsInGraveyard() {
        harness.addToBattlefield(player1, new AngelOfFlightAlabaster());
        harness.setGraveyard(player1, List.of(new AbbeyGriffin()));

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Abbey Griffin");
    }

    @Test
    @DisplayName("Upkeep trigger does not fire during the opponent's upkeep")
    void upkeepTriggerDoesNotFireDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new AngelOfFlightAlabaster());
        harness.setGraveyard(player1, List.of(new VoicelessSpirit()));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Voiceless Spirit");
    }

    @Test
    @DisplayName("Targets only Spirit cards in the controller's graveyard")
    void filtersTargetsBySubtypeAndGraveyardOwner() {
        VoicelessSpirit spirit = new VoicelessSpirit();
        harness.addToBattlefield(player1, new AngelOfFlightAlabaster());
        harness.setGraveyard(player1, List.of(new AbbeyGriffin(), spirit));
        harness.setGraveyard(player2, List.of(new ChapelGeist()));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        PendingInteraction.MultiGraveyardChoice choice =
                (PendingInteraction.MultiGraveyardChoice) gd.interaction.activeInteraction();
        assertThat(choice.cards()).containsExactly(spirit);
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Voiceless Spirit");
        harness.assertInGraveyard(player1, "Abbey Griffin");
        harness.assertInGraveyard(player2, "Chapel Geist");
    }

    @Test
    @DisplayName("The controller cannot decline to choose a legal Spirit target")
    void cannotDeclineMandatoryTarget() {
        VoicelessSpirit spirit = new VoicelessSpirit();
        harness.addToBattlefield(player1, new AngelOfFlightAlabaster());
        harness.setGraveyard(player1, List.of(spirit));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Voiceless Spirit");
    }

    @Test
    @DisplayName("An exiled target is not returned or replaced with another Spirit")
    void targetLeavingGraveyardDoesNotAllowReselection() {
        VoicelessSpirit target = new VoicelessSpirit();
        ChapelGeist otherSpirit = new ChapelGeist();
        harness.addToBattlefield(player1, new AngelOfFlightAlabaster());
        harness.setGraveyard(player1, List.of(target, otherSpirit));

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(otherSpirit));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Voiceless Spirit");
        harness.assertNotInHand(player1, "Chapel Geist");
        harness.assertInGraveyard(player1, "Chapel Geist");
        assertThat(gd.findExiledCard(target.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
