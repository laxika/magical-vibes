package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.PreventManaDrainEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UpwellingTest extends BaseCardTest {


    @Test
    @DisplayName("Upwelling has correct card properties")
    void hasCorrectProperties() {
        Upwelling card = new Upwelling();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(PreventManaDrainEffect.class);
    }

    @Test
    @DisplayName("Mana is preserved when step advances with Upwelling on battlefield")
    void manaPreservedOnStepAdvance() {
        harness.addToBattlefield(player1, new Upwelling());
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.getGameService().advanceStep(gd);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's mana is also preserved by Upwelling")
    void opponentManaAlsoPreserved() {
        harness.addToBattlefield(player1, new Upwelling());
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.getGameService().advanceStep(gd);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Without Upwelling mana drains normally on step advance")
    void manaDrainsNormallyWithoutUpwelling() {
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.getGameService().advanceStep(gd);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("Mana drains again after Upwelling is removed")
    void manaDrainsAfterUpwellingRemoved() {
        harness.addToBattlefield(player1, new Upwelling());
        harness.addMana(player1, ManaColor.GREEN, 3);

        // Remove Upwelling from the battlefield
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.getGameService().advanceStep(gd);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("Upwelling controlled by opponent still preserves your mana")
    void opponentControlledUpwellingPreservesYourMana() {
        harness.addToBattlefield(player2, new Upwelling());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.getGameService().advanceStep(gd);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(5);
    }
}
