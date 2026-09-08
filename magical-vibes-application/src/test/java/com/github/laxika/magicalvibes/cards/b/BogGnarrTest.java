package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BogGnarr.class, BlackCat.class, GrizzlyBears.class})
class BogGnarrTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+2 when any player casts a black spell")
    void getsBoostWhenAnyPlayerCastsBlackSpell() {
        Permanent gnarr = addGnarr();

        castBlackSpell(player2);
        harness.passBothPriorities();

        assertThat(gnarr.getPowerModifier()).isEqualTo(2);
        assertThat(gnarr.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for a nonblack spell")
    void doesNotTriggerForNonblackSpell() {
        Permanent gnarr = addGnarr();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        castSpellAsOpponent();

        assertThat(gnarr.getPowerModifier()).isZero();
        assertThat(gnarr.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Each black spell cast gives another +2/+2")
    void blackSpellTriggersStack() {
        Permanent gnarr = addGnarr();

        castBlackSpell(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        castBlackSpell(player1);
        harness.passBothPriorities();

        assertThat(gnarr.getPowerModifier()).isEqualTo(4);
        assertThat(gnarr.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent gnarr = addGnarr();

        castBlackSpell(player2);
        harness.passBothPriorities();
        assertThat(gnarr.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gnarr.getPowerModifier()).isZero();
        assertThat(gnarr.getToughnessModifier()).isZero();
    }

    private Permanent addGnarr() {
        return harness.addToBattlefieldAndReturn(player1, new BogGnarr());
    }

    private void castBlackSpell(com.github.laxika.magicalvibes.model.Player caster) {
        harness.setHand(caster, List.of(new BlackCat()));
        harness.addMana(caster, ManaColor.BLACK, 2);
        if (caster == player2) {
            castSpellAsOpponent();
        } else {
            harness.castCreature(caster, 0);
        }
    }

    private void castSpellAsOpponent() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player2, 0);
    }
}
