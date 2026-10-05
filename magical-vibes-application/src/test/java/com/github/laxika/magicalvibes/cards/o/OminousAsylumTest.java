package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OminousAsylum.class})
class OminousAsylumTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        Card card = new OminousAsylum();
        harness.setHand(player1, List.of(card));

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == card && permanent.isTapped());
    }

    @Test
    @DisplayName("Adds black mana")
    void addsBlackMana() {
        addsMana(ManaColor.BLACK);
    }

    @Test
    @DisplayName("Adds red mana")
    void addsRedMana() {
        addsMana(ManaColor.RED);
    }

    @Test
    @DisplayName("Pays four mana and taps to surveil 1")
    void paysFourManaAndSurveils() {
        Card topCard = new OminousAsylum();
        harness.setLibrary(player1, List.of(topCard));
        Permanent land = addLandReady();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(land.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Declining surveil leaves the library in its original order")
    void decliningSurveilKeepsTopCard() {
        Card topCard = new OminousAsylum();
        Card nextCard = new OminousAsylum();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        addLandReady();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Surveil moves only the controller's top card")
    void surveilsOnlyControllersTopCard() {
        Card topCard = new OminousAsylum();
        Card nextCard = new OminousAsylum();
        Card opponentsCard = new OminousAsylum();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setLibrary(player2, List.of(opponentsCard));
        addLandReady();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Surveil resolves with an empty library without asking for a choice")
    void surveilsEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        Permanent land = addLandReady();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Surveil cannot be activated with only three mana")
    void cannotSurveilWithoutFourMana() {
        Permanent land = addLandReady();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(land.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped asylum cannot activate either ability")
    void tappedLandCannotActivateAbilities() {
        Permanent land = addLandReady();
        land.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addLandReady() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new OminousAsylum());
        land.setSummoningSick(false);
        return land;
    }

    private void addsMana(ManaColor color) {
        Permanent land = addLandReady();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }
}
