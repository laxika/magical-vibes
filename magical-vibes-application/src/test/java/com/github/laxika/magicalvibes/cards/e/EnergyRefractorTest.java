package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnergyRefractorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield and draws a card")
    void entersBattlefieldDrawsACard() {
        harness.setHand(player1, List.of(new EnergyRefractor()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        GameData gd = harness.getGameData();
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castArtifact(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 1);
    }

    @Test
    @DisplayName("Pays {2}, prompts for a color, and adds one mana without tapping")
    void abilityAddsChosenColorWithoutTapping() {
        harness.addToBattlefield(player1, new EnergyRefractor());
        Permanent refractor = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(refractor.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot activate the mana ability without paying {2}")
    void abilityRequiresTwoMana() {
        harness.addToBattlefield(player1, new EnergyRefractor());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
