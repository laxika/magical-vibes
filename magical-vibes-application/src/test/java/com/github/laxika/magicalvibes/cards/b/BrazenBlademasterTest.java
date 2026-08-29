package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrazenBlademaster.class, LeoninScimitar.class, Spellbook.class})
class BrazenBlademasterTest extends BaseCardTest {

    @Test
    @DisplayName("Does not get a boost with fewer than two artifacts")
    void noBoostWithFewerThanTwoArtifacts() {
        Permanent blademaster = addBlademaster();
        harness.addToBattlefield(player1, new Spellbook());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, blademaster)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blademaster)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +2/+1 when attacking with two artifacts")
    void getsBoostWithTwoArtifacts() {
        Permanent blademaster = addBlademaster();
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, blademaster)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, blademaster)).isEqualTo(4);
    }

    @Test
    @DisplayName("Keeps the boost if an artifact is removed after attacking")
    void artifactCountIsCheckedWhenAttacking() {
        Permanent blademaster = addBlademaster();
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        declareAttackers(player1, List.of(0));
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Spellbook"));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, blademaster)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, blademaster)).isEqualTo(4);
    }

    @Test
    @DisplayName("Opponent's artifacts do not count")
    void opponentArtifactsDoNotCount() {
        Permanent blademaster = addBlademaster();
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new LeoninScimitar());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, blademaster)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blademaster)).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost lasts until end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent blademaster = addBlademaster();
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, blademaster)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blademaster)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blademaster)).isEqualTo(3);
    }

    private Permanent addBlademaster() {
        return addCreatureReady(player1, new BrazenBlademaster());
    }
}
