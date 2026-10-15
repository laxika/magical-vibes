package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.s.SpinelessThug;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EyeOfYawgmoth.class, SpinelessThug.class})
class EyeOfYawgmothTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature, then puts one card from its power-sized reveal into hand and exiles the rest")
    void sacrificesCreatureAndChoosesFromPowerSizedReveal() {
        Permanent eye = addReadyEye();
        Permanent creature = addCreatureReady(player1, new SpinelessThug());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        Card first = new SpinelessThug();
        Card chosen = new SpinelessThug();
        Card third = new SpinelessThug();
        harness.setLibrary(player1, List.of(first, chosen, third));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(eye.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Spineless Thug");
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice interaction =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(interaction).isNotNull();
        assertThat(interaction.allCards()).containsExactly(first, chosen, third);

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(first, third);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("With an empty library, sacrifices the creature without creating a card choice")
    void emptyLibraryCompletesWithoutCardChoice() {
        addReadyEye();
        addCreatureReady(player1, new SpinelessThug());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spineless Thug");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void requiresCreatureToSacrifice() {
        addReadyEye();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Reveals all cards publicly before the controller chooses one")
    void publiclyRevealsBeforeChoice() {
        addReadyEye();
        addCreatureReady(player1, new SpinelessThug());
        harness.setLibrary(player1, List.of(new EyeOfYawgmoth(), new SpinelessThug()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.gameLog).anySatisfy(entry -> {
            assertThat(entry.plainText()).contains("reveals", "Eye of Yawgmoth", "Spineless Thug");
        });
    }

    @Test
    @DisplayName("A library shorter than the sacrificed creature's power reveals its only card and puts it into hand")
    void shortLibraryRevealsAndKeepsOnlyCard() {
        addReadyEye();
        addCreatureReady(player1, new SpinelessThug());
        Card onlyCard = new EyeOfYawgmoth();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(onlyCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog).anySatisfy(entry -> {
            assertThat(entry.plainText()).contains("reveals", "Eye of Yawgmoth");
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {-2, -3})
    @DisplayName("Sacrificing a creature with zero or negative power does not reveal or move library cards")
    void nonpositivePowerLeavesLibraryUnchanged(int powerModifier) {
        addReadyEye();
        Permanent creature = addCreatureReady(player1, new SpinelessThug());
        creature.setPowerModifier(powerModifier);
        Card first = new EyeOfYawgmoth();
        Card second = new SpinelessThug();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spineless Thug");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller must choose exactly one of the revealed cards")
    void cannotChooseZeroOrMultipleCards() {
        addReadyEye();
        addCreatureReady(player1, new SpinelessThug());
        Card first = new EyeOfYawgmoth();
        Card second = new SpinelessThug();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(second);
    }

    @Test
    @DisplayName("Choosing among creatures uses the chosen creature's power and leaves deeper library cards alone")
    void chosenSacrificeDeterminesRevealSize() {
        addReadyEye();
        Permanent other = addCreatureReady(player1, new SpinelessThug());
        other.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent chosenCreature = harness.addToBattlefieldAndReturn(player1, new SpinelessThug());
        chosenCreature.tap();
        Card first = new EyeOfYawgmoth();
        Card second = new SpinelessThug();
        Card third = new EyeOfYawgmoth();
        Card fourth = new SpinelessThug();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, chosenCreature.getId());
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice interaction =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(interaction).isNotNull();
        assertThat(interaction.allCards()).containsExactly(first, second);

        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(other).doesNotContain(chosenCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(chosenCreature.getCard());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, fourth);
    }

    @Test
    @DisplayName("Cannot activate a tapped Eye")
    void cannotActivateWhileTapped() {
        Permanent eye = addReadyEye();
        eye.tap();
        addCreatureReady(player1, new SpinelessThug());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Spineless Thug");
    }

    @Test
    @DisplayName("Cannot activate without three mana")
    void cannotActivateWithInsufficientMana() {
        Permanent eye = addReadyEye();
        addCreatureReady(player1, new SpinelessThug());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(eye.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Spineless Thug");
    }

    private Permanent addReadyEye() {
        Permanent eye = harness.addToBattlefieldAndReturn(player1, new EyeOfYawgmoth());
        eye.setSummoningSick(false);
        return eye;
    }

}
