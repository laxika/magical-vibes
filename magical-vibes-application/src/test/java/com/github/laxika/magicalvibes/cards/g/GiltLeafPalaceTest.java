package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.ElvishEulogist;
import com.github.laxika.magicalvibes.cards.e.EyeblightsEnding;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GiltLeafPalace.class, ElvishEulogist.class, EyeblightsEnding.class, Forest.class, WoodlandChangeling.class})
class GiltLeafPalaceTest extends BaseCardTest {


    @Test
    @DisplayName("Enters tapped when you have no Elf card in hand")
    void entersTappedWithoutElf() {
        harness.setHand(player1, List.of(new GiltLeafPalace(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent land = findLand(player1);
        assertThat(land.isTapped()).isTrue();
    }


    @Test
    @DisplayName("Revealing an Elf lets it enter untapped")
    void entersUntappedWhenRevealing() {
        ElvishEulogist elf = new ElvishEulogist();
        harness.setHand(player1, List.of(new GiltLeafPalace(), elf));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        Permanent land = findLand(player1);
        assertThat(land.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(elf);
    }

    @Test
    @DisplayName("Declining to reveal makes it enter tapped even with an Elf in hand")
    void entersTappedWhenDeclining() {
        harness.setHand(player1, List.of(new GiltLeafPalace(), new ElvishEulogist()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        Permanent land = findLand(player1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A changeling card can be revealed as an Elf")
    void entersUntappedWhenRevealingChangeling() {
        WoodlandChangeling changeling = new WoodlandChangeling();
        harness.setHand(player1, List.of(new GiltLeafPalace(), changeling));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand(player1).isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(changeling);
    }

    @Test
    @DisplayName("A noncreature Elf card can be revealed")
    void entersUntappedWhenRevealingKindredInstant() {
        EyeblightsEnding elf = new EyeblightsEnding();
        harness.setHand(player1, List.of(new GiltLeafPalace(), elf));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand(player1).isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(elf);
    }

    @Test
    @DisplayName("Elves on the battlefield or in an opponent's hand cannot be revealed")
    void entersTappedWhenElvesAreOutsideControllersHand() {
        harness.addToBattlefield(player1, new ElvishEulogist());
        harness.setHand(player2, List.of(new ElvishEulogist()));
        harness.setHand(player1, List.of(new GiltLeafPalace()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findLand(player1).isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Tapping for black mana produces one black")
    void tappingProducesBlackMana() {
        harness.addToBattlefield(player1, new GiltLeafPalace());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingProducesGreenMana() {
        harness.addToBattlefield(player1, new GiltLeafPalace());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    private Permanent findLand(Player player) {
        return findPermanent(player, "Gilt-Leaf Palace");
    }
}
