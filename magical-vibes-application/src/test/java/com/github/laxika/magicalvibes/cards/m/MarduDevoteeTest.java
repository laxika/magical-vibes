package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarduDevotee.class})
class MarduDevoteeTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield lets you scry 2")
    void enteringBattlefieldScriesTwo() {
        harness.setLibrary(player1, List.of(new MarduDevotee(), new MarduDevotee(), new MarduDevotee()));
        castMarduDevotee();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The mana ability adds a chosen Mardu color")
    void manaAbilityAddsChosenColor() {
        harness.addToBattlefield(player1, new MarduDevotee());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("The mana ability can be activated only once each turn")
    void manaAbilityOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new MarduDevotee());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castMarduDevotee() {
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new MarduDevotee(), "{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLACK"})
    @DisplayName("The other Mardu colors are available immediately without tapping")
    void manaAbilityAddsOtherColorsWithoutUsingStack(ManaColor color) {
        var permanent = harness.addToBattlefieldAndReturn(player1, new MarduDevotee());
        permanent.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(permanent.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The activation allowance resets on the opponent's turn")
    void manaAbilityCanBeUsedOnNextTurn() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new MarduDevotee());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Each Devotee has its own activation allowance")
    void manaAbilityLimitIsPerPermanent() {
        harness.addToBattlefield(player1, new MarduDevotee());
        harness.addToBattlefield(player1, new MarduDevotee());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");
        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Activation requires paying one mana")
    void manaAbilityCannotBeActivatedWithoutMana() {
        harness.addToBattlefield(player1, new MarduDevotee());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    @DisplayName("Scry can reorder both cards or put either number on the bottom")
    void scryOrdersTopAndBottomCards(int bottomCount) {
        var first = new MarduDevotee();
        var second = new MarduDevotee();
        var third = new MarduDevotee();
        harness.setLibrary(player1, List.of(first, second, third));
        castMarduDevotee();

        List<Integer> top = switch (bottomCount) {
            case 0 -> List.of(1, 0);
            case 1 -> List.of(1);
            default -> List.of();
        };
        List<Integer> bottom = switch (bottomCount) {
            case 0 -> List.of();
            case 1 -> List.of(0);
            default -> List.of(1, 0);
        };
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(top, bottom));

        var expected = switch (bottomCount) {
            case 0 -> List.of(second, first, third);
            case 1 -> List.of(second, third, first);
            default -> List.of(third, second, first);
        };
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(expected);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Scry with one card offers only that card")
    void scryWithOneCard() {
        var card = new MarduDevotee();
        harness.setLibrary(player1, List.of(card));
        castMarduDevotee();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(card);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Scry with an empty library completes without a choice")
    void scryWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        castMarduDevotee();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Mardu Devotee");
    }
}
