package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LetterOfAcceptance.class})
class LetterOfAcceptanceTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one mana of a chosen color")
    void tappingAddsChosenColor() {
        Permanent letter = harness.addToBattlefieldAndReturn(player1, new LetterOfAcceptance());

        harness.activateAbility(player1, 0, null, null);

        assertThat(letter.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Pays {2}, sacrifices itself, and draws a card")
    void sacrificesAndDrawsCard() {
        harness.addToBattlefield(player1, new LetterOfAcceptance());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        GameData gd = harness.getGameData();
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof LetterOfAcceptance);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 1);
    }

    @Test
    @DisplayName("The draw ability requires two mana")
    void drawAbilityRequiresTwoMana() {
        harness.addToBattlefield(player1, new LetterOfAcceptance());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void manaAbilityResolvesImmediatelyForEveryColor(ManaColor color) {
        harness.addToBattlefield(player1, new LetterOfAcceptance());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(color)).isZero();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void drawAcceptsColoredManaAndPaysCostsBeforeResolving(ManaColor color) {
        harness.addToBattlefield(player1, new LetterOfAcceptance());
        harness.setHand(player1, List.of());
        LetterOfAcceptance drawnCard = new LetterOfAcceptance();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, color, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void insufficientManaDoesNotTapOrSacrificeLetter() {
        Permanent letter = harness.addToBattlefieldAndReturn(player1, new LetterOfAcceptance());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(letter.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(letter);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedLetterCannotActivateEitherAbility() {
        Permanent letter = harness.addToBattlefieldAndReturn(player1, new LetterOfAcceptance());
        letter.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(letter);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
