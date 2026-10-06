package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrislySpectacle;
import com.github.laxika.magicalvibes.model.Keyword;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HorrorOfTheDim.class, GrislySpectacle.class})
class HorrorOfTheDimTest extends BaseCardTest {

    @Test
    @DisplayName("{U} ability grants Horror of the Dim hexproof until end of turn")
    void abilityGrantsHexproofUntilEndOfTurn() {
        Permanent horror = addHorrorReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, horror, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Hexproof from the ability wears off at end of turn")
    void hexproofWearsOffAtEndOfTurn() {
        Permanent horror = addHorrorReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, horror, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("The ability requires one blue mana")
    void abilityRequiresBlueMana() {
        addHorrorReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The ability can be activated while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent horror = harness.addToBattlefieldAndReturn(player1, new HorrorOfTheDim());
        horror.setSummoningSick(true);
        horror.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, horror, Keyword.HEXPROOF)).isTrue();
        assertThat(horror.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Hexproof prevents an opponent from targeting the creature")
    void opponentCannotTargetAfterAbilityResolves() {
        Permanent horror = addHorrorReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new GrislySpectacle()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, horror.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
        harness.assertOnBattlefield(player1, "Horror of the Dim");
    }

    @Test
    @DisplayName("Gaining hexproof in response makes an opposing spell's target illegal")
    void hexproofInResponseStopsRemoval() {
        Permanent horror = addHorrorReady(player1);
        harness.setHand(player2, List.of(new GrislySpectacle()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.castInstant(player2, 0, horror.getId());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, horror, Keyword.HEXPROOF)).isTrue();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Horror of the Dim");
        harness.assertInGraveyard(player2, "Grisly Spectacle");
    }

    @Test
    @DisplayName("Hexproof still allows its controller to target the creature")
    void controllerCanStillTargetCreature() {
        Permanent horror = addHorrorReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new GrislySpectacle()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, horror.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Horror of the Dim");
        harness.assertInGraveyard(player1, "Horror of the Dim");
    }

    private Permanent addHorrorReady(Player player) {
        Permanent horror = harness.addToBattlefieldAndReturn(player, new HorrorOfTheDim());
        horror.setSummoningSick(false);
        return horror;
    }
}
