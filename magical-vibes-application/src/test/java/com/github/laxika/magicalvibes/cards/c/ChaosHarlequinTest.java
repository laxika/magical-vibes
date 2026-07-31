package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Chaos Harlequin")
class ChaosHarlequinTest extends BaseCardTest {

    @Test
    @DisplayName("Gets -4/-0 when the exiled card is a land")
    void shrinksOnLand() {
        Permanent harlequin = addHarlequin();
        gd.playerDecks.get(player1.getId()).addFirst(new Mountain());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, harlequin)).isEqualTo(-2);
        assertThat(gqs.getEffectiveToughness(gd, harlequin)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets +2/+0 when the exiled card is not a land")
    void pumpsOnNonland() {
        Permanent harlequin = addHarlequin();
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, harlequin)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, harlequin)).isEqualTo(4);
    }

    @Test
    @DisplayName("Exiles the top card of the library")
    void exilesTopCard() {
        addHarlequin();
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        int deckBefore = gd.playerDecks.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent harlequin = addHarlequin();
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, harlequin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, harlequin)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot be activated with an empty library")
    void cannotActivateWithEmptyLibrary() {
        addHarlequin();
        gd.playerDecks.get(player1.getId()).clear();
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough cards in library to exile");
    }

    private Permanent addHarlequin() {
        Permanent harlequin = new Permanent(new ChaosHarlequin());
        harlequin.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(harlequin);
        return harlequin;
    }
}
