package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PitilessPontiff.class})
class PitilessPontiffTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices another creature and gains deathtouch and indestructible")
    void sacrificesAnotherCreatureAndGainsKeywords() {
        Permanent pontiff = addReadyPontiff(player1);
        harness.addToBattlefield(player1, new PitilessPontiff());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Pitiless Pontiff");
        assertThat(gqs.hasKeyword(gd, pontiff, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, pontiff, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, pontiff, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, pontiff, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertInGraveyard(player1, "Pitiless Pontiff");
        harness.assertOnBattlefield(player1, "Pitiless Pontiff");
    }

    @Test
    @DisplayName("Granted keywords wear off at end of turn")
    void grantedKeywordsWearOffAtEndOfTurn() {
        Permanent pontiff = addReadyPontiff(player1);
        harness.addToBattlefield(player1, new PitilessPontiff());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, pontiff, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, pontiff, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without another creature to sacrifice")
    void cannotActivateWithoutAnotherCreature() {
        addReadyPontiff(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature")
    void cannotSacrificeOpponentsCreature() {
        addReadyPontiff(player1);
        harness.addToBattlefield(player2, new PitilessPontiff());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Pitiless Pontiff");
        harness.assertOnBattlefield(player2, "Pitiless Pontiff");
    }

    @Test
    @DisplayName("Cannot activate without paying one mana")
    void cannotActivateWithoutMana() {
        addReadyPontiff(player1);
        harness.addToBattlefield(player1, new PitilessPontiff());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(countPermanents(player1, "Pitiless Pontiff")).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Pitiless Pontiff");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Pontiff can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent pontiff = harness.addToBattlefieldAndReturn(player1, new PitilessPontiff());
        pontiff.setSummoningSick(true);
        pontiff.setTapped(true);
        harness.addToBattlefield(player1, new PitilessPontiff());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, pontiff, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, pontiff, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(pontiff);
        harness.assertInGraveyard(player1, "Pitiless Pontiff");
    }

    private Permanent addReadyPontiff(Player player) {
        Permanent pontiff = harness.addToBattlefieldAndReturn(player, new PitilessPontiff());
        pontiff.setSummoningSick(false);
        return pontiff;
    }
}
