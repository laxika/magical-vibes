package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({GladeGnarr.class, FugitiveWizard.class, GrizzlyBears.class})
class GladeGnarrTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+2 when any player casts a blue spell")
    void getsBoostWhenAnyPlayerCastsBlueSpell() {
        Permanent gnarr = addGnarr();

        castBlueSpell(player2);
        harness.passBothPriorities();

        assertThat(gnarr.getPowerModifier()).isEqualTo(2);
        assertThat(gnarr.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for a nonblue spell")
    void doesNotTriggerForNonblueSpell() {
        Permanent gnarr = addGnarr();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        castSpellAsOpponent();

        assertThat(gnarr.getPowerModifier()).isZero();
        assertThat(gnarr.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Each blue spell cast gives another +2/+2")
    void blueSpellTriggersStack() {
        Permanent gnarr = addGnarr();

        castBlueSpell(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        castBlueSpell(player1);
        harness.passBothPriorities();

        assertThat(gnarr.getPowerModifier()).isEqualTo(4);
        assertThat(gnarr.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent gnarr = addGnarr();

        castBlueSpell(player2);
        harness.passBothPriorities();
        assertThat(gnarr.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gnarr.getPowerModifier()).isZero();
        assertThat(gnarr.getToughnessModifier()).isZero();
    }

    private Permanent addGnarr() {
        return harness.addToBattlefieldAndReturn(player1, new GladeGnarr());
    }

    private void castBlueSpell(Player caster) {
        harness.setHand(caster, List.of(new FugitiveWizard()));
        harness.addMana(caster, ManaColor.BLUE, 1);
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
