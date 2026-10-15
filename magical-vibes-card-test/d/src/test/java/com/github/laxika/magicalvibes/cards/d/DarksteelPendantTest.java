package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EchoingRuin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarksteelPendant.class, EchoingRuin.class})
class DarksteelPendantTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability spends one mana and taps Darksteel Pendant")
    void activatingSpendsManaAndTapsPendant() {
        Permanent pendant = addReadyPendant();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(pendant.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Resolving the ability scries one card")
    void resolvingScriesOneCard() {
        addReadyPendant();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .hasSize(1);
    }

    @Test
    @DisplayName("Bottoming the scried card moves it to the bottom of the library")
    void scryBottom() {
        addReadyPendant();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top = deck.getFirst();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(deck.getFirst()).isNotSameAs(top);
        assertThat(deck.getLast()).isSameAs(top);
    }

    @Test
    @DisplayName("Keeping the scried card leaves it on top of the library")
    void scryKeep() {
        addReadyPendant();
        List<Card> deck = List.of(new DarksteelPendant(), new DarksteelPendant());
        harness.setLibrary(player1, deck);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(deck);
    }

    @Test
    @DisplayName("Scrying an empty library resolves without a choice")
    void scryEmptyLibraryResolvesWithoutChoice() {
        addReadyPendant();
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability cannot be activated without one mana")
    void cannotActivateWithoutMana() {
        addReadyPendant();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Darksteel Pendant survives an artifact destruction spell")
    void survivesArtifactDestruction() {
        Permanent pendant = harness.addToBattlefieldAndReturn(player1, new DarksteelPendant());
        harness.setHand(player1, List.of(new EchoingRuin()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, pendant.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(pendant);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .doesNotContain(pendant.getCard());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A newly entered noncreature Pendant can activate its tap ability")
    void canActivateImmediatelyAfterEntering() {
        Permanent pendant = harness.addToBattlefieldAndReturn(player1, new DarksteelPendant());
        harness.setLibrary(player1, List.of(new DarksteelPendant()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(pendant.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("A tapped Pendant cannot activate or spend mana")
    void cannotActivateWhileTapped() {
        Permanent pendant = addReadyPendant();
        pendant.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyPendant() {
        Permanent pendant = harness.addToBattlefieldAndReturn(player1, new DarksteelPendant());
        pendant.setSummoningSick(false);
        return pendant;
    }
}
