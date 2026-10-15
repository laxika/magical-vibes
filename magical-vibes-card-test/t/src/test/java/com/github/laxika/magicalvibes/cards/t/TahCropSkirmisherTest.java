package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TahCropSkirmisher.class})
class TahCropSkirmisherTest extends BaseCardTest {

    private void setUpEmbalm() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new TahCropSkirmisher()));
        harness.addMana(player1, ManaColor.BLUE, 4); // pays {3}{U}
    }

    @Test
    @DisplayName("Embalm exiles the source card from the graveyard as a cost")
    void embalmExilesSourceAsCost() {
        setUpEmbalm();

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Tah-Crop Skirmisher");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Tah-Crop Skirmisher"));
    }

    @Test
    @DisplayName("Embalm creates a white Zombie Snake Warrior token copy with no mana cost")
    void embalmCreatesWhiteZombieTokenCopy() {
        setUpEmbalm();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities(); // resolve the Embalm ability

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Tah-Crop Skirmisher") && p.getCard().isToken())
                .findFirst().orElseThrow();

        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getColors()).contains(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes())
                .contains(CardSubtype.ZOMBIE, CardSubtype.SNAKE, CardSubtype.WARRIOR);
        assertThat(token.getCard().getManaCost()).isEmpty();
    }

    @Test
    @DisplayName("Embalm can only be activated at sorcery speed")
    void embalmOnlyAtSorcerySpeed() {
        harness.setGraveyard(player1, List.of(new TahCropSkirmisher()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        // Opponent's turn — not sorcery speed for player1.
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Assertions.assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Tah-Crop Skirmisher");
    }

    @Test
    @DisplayName("Embalm cannot be activated during combat on your own turn")
    void embalmCannotBeActivatedDuringCombat() {
        setUpEmbalm();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        Assertions.assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Tah-Crop Skirmisher");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Embalm requires an empty stack even during your main phase")
    void embalmCannotBeActivatedWithNonemptyStack() {
        setUpEmbalm();
        harness.setGraveyard(player1, List.of(new TahCropSkirmisher(), new TahCropSkirmisher()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.activateGraveyardAbility(player1, 0);

        Assertions.assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player1, "Tah-Crop Skirmisher");
        harness.assertNotOnBattlefield(player1, "Tah-Crop Skirmisher");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Tah-Crop Skirmisher");
    }

    @Test
    @DisplayName("Insufficient mana for embalm leaves the source in the graveyard")
    void embalmRequiresFourMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new TahCropSkirmisher()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        Assertions.assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Tah-Crop Skirmisher");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Tah-Crop Skirmisher"));
    }
}
