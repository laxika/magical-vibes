package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SurveillanceRoom.class})
class SurveillanceRoomTest extends BaseCardTest {

    @Test
    void entersAndSurveilsOne() {
        Card topCard = new SurveillanceRoom();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new SurveillanceRoom()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void tapsForColorlessMana() {
        Permanent room = addReadyRoom();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(room.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void paysOneAndTapsForAnyColorMana() {
        Permanent room = addReadyRoom();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(room.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotPayAnyColorAbilityWithoutMana() {
        addReadyRoom();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyRoom() {
        Permanent room = harness.addToBattlefieldAndReturn(player1, new SurveillanceRoom());
        room.setSummoningSick(false);
        return room;
    }

    @Test
    void canLeaveSurveilledCardOnTopWithoutChangingLibraryOrder() {
        Card topCard = new SurveillanceRoom();
        Card nextCard = new SurveillanceRoom();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setHand(player1, List.of(new SurveillanceRoom()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void surveilsOnlyTheTopCard() {
        Card topCard = new SurveillanceRoom();
        Card nextCard = new SurveillanceRoom();
        Card opponentCard = new SurveillanceRoom();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setLibrary(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new SurveillanceRoom()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void emptyLibraryNeedsNoSurveilChoice() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new SurveillanceRoom()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void canFilterColorlessManaIntoEachColor(ManaColor color) {
        Permanent room = addReadyRoom();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(room.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void tappedLandCannotActivateEitherManaAbility(int abilityIndex) {
        Permanent room = addReadyRoom();
        room.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void newlyPlayedLandCanProduceManaBeforeSurveilResolves() {
        Card topCard = new SurveillanceRoom();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new SurveillanceRoom()));

        harness.withAutoStop(gd.currentStep, () -> {
            harness.playLand(player1, 0);
            assertThat(gd.stack).hasSize(1);

            harness.activateAbility(player1, 0, 0, null, null);

            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        });

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
    }
}
