package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoxTantalite.class, PithingNeedle.class})
class MoxTantaliteTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Mox Tantalite with three time counters")
    void suspendExilesWithThreeTimeCounters() {
        MoxTantalite card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The last suspend counter offers a free cast")
    void lastCounterOffersFreeCast() {
        MoxTantalite card = suspendCard();

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mox Tantalite");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Tapping Mox Tantalite adds one mana of the chosen color")
    void tappingAddsManaOfChosenColor(ManaColor color) {
        MoxTantalite mox = new MoxTantalite();
        harness.addToBattlefield(player1, mox);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanent(player1, "Mox Tantalite").isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Declining the suspend cast leaves Mox Tantalite exiled without another offer")
    void decliningFreeCastLeavesCardExiled() {
        MoxTantalite card = suspendCard();
        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        harness.assertNotOnBattlefield(player1, "Mox Tantalite");
        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent's upkeep does not remove a suspend counter")
    void opponentUpkeepDoesNotRemoveCounter() {
        MoxTantalite card = suspendCard();

        advanceToUpkeep(player2);

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Pithing Needle cannot prevent the suspend special action")
    void pithingNeedleDoesNotPreventSuspend() {
        harness.setHand(player1, List.of(new PithingNeedle()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Mox Tantalite");

        MoxTantalite card = suspendCard();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
        assertThat(gd.stack).isEmpty();
    }

    private MoxTantalite suspendCard() {
        MoxTantalite card = new MoxTantalite();
        harness.setHand(player1, List.of(card));
        harness.activateHandAbility(player1, 0, null);
        return card;
    }
}
