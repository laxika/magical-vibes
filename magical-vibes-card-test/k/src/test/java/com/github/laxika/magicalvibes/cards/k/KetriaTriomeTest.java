package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({KetriaTriome.class, GrizzlyBears.class})
class KetriaTriomeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new KetriaTriome()));

        harness.playLand(player1, 0);

        Permanent triome = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(triome.isTapped()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"GREEN", "BLUE", "RED"})
    @DisplayName("Mana ability adds the chosen color")
    void addsChosenManaColor(String color) {
        Permanent triome = addCreatureReady(player1, new KetriaTriome());

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        ManaColor manaColor = ManaColor.valueOf(color);
        harness.handleListChoice(player1, color);

        assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(1);
        assertThat(triome.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cycling discards it and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new KetriaTriome()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ketria Triome");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate the mana ability while tapped")
    void cannotActivateManaAbilityWhileTapped() {
        harness.setHand(player1, List.of(new KetriaTriome()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }
    }

    @Test
    @DisplayName("Cycling requires three mana and leaves the card in hand if unpaid")
    void cyclingRequiresThreeMana() {
        KetriaTriome triome = new KetriaTriome();
        harness.setHand(player1, List.of(triome));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(triome);
        harness.assertNotInGraveyard(player1, "Ketria Triome");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling pays and discards immediately but draws only on resolution")
    void cyclingCostsArePaidBeforeResolution() {
        KetriaTriome cycled = new KetriaTriome();
        KetriaTriome drawn = new KetriaTriome();
        harness.setHand(player1, List.of(cycled));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(cycled);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.stack).hasSize(1);
        for (ManaColor color : List.of(ManaColor.GREEN, ManaColor.BLUE, ManaColor.RED)) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(cycled);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
