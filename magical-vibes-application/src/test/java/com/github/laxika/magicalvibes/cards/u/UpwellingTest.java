package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Upwelling.class, SongOfTheDryads.class})
class UpwellingTest extends BaseCardTest {

    @Test
    @DisplayName("Both players keep unspent mana across the turn boundary")
    void manaPreservedAcrossTurns() {
        harness.addToBattlefield(player1, new Upwelling());
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(4);
    }

    @Test
    @CardUsed({Upwelling.class, SongOfTheDryads.class})
    @DisplayName("Upwelling turned into a Forest no longer preserves mana")
    void manaDrainsWhenUpwellingLosesItsPrintedAbility() {
        Permanent upwelling = harness.addToBattlefieldAndReturn(player1, new Upwelling());
        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, upwelling.getId());
        harness.passBothPriorities();
        assertThat(gqs.isLand(gd, upwelling)).isTrue();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isZero();
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
    @DisplayName("Mana is preserved when a phase ends with Upwelling on the battlefield")
    void manaPreservedOnPhaseEnd() {
        harness.addToBattlefield(player1, new Upwelling());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.getGameService().advanceStep(gd);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
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
