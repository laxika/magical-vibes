package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantMantis;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CadaverousBloom.class, Forest.class, GiantMantis.class})
class CadaverousBloomTest extends BaseCardTest {

    @Test
    @DisplayName("Activating starts a hand-card choice with every card legal")
    void activationStartsHandChoice() {
        harness.addToBattlefield(player1, new CadaverousBloom());
        harness.setHand(player1, List.of(new GiantMantis(), new Forest()));

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(0, 1);
    }

    @Test
    @DisplayName("First ability exiles the chosen card and adds {B}{B}")
    void firstAbilityAddsBlackMana() {
        harness.addToBattlefield(player1, new CadaverousBloom());
        harness.setHand(player1, List.of(new GiantMantis(), new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInHand(player1, "Giant Mantis");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(e -> e.card().getName()).containsExactly("Giant Mantis");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        // Mana ability — never uses the stack (CR 605.3b).
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Second ability adds {G}{G} instead")
    void secondAbilityAddsGreenMana() {
        harness.addToBattlefield(player1, new CadaverousBloom());
        harness.setHand(player1, List.of(new GiantMantis()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can activate both mana abilities in succession")
    void canActivateBothManaAbilitiesInSuccession() {
        harness.addToBattlefield(player1, new CadaverousBloom());
        harness.setHand(player1, List.of(new GiantMantis(), new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.exiledCards).extracting(e -> e.card().getName())
                .containsExactly("Giant Mantis", "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate with an empty hand")
    void cannotActivateWithEmptyHand() {
        harness.addToBattlefield(player1, new CadaverousBloom());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exile");
    }
}
